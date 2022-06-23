#include "SchedulerImpl.h"
#include "Common.h"
#include <bcos-framework//ledger/LedgerConfig.h>
#include <bcos-framework//protocol/GlobalConfig.h>
#include <bcos-framework//protocol/ProtocolTypeDef.h>
#include <bcos-tool/VersionConverter.h>
#include <bcos-utilities/Error.h>
#include <boost/exception/diagnostic_information.hpp>
#include <boost/format.hpp>
#include <boost/lexical_cast.hpp>
#include <boost/throw_exception.hpp>
#include <memory>
#include <mutex>
#include <variant>

using namespace bcos::scheduler;


void SchedulerImpl::executeBlock(bcos::protocol::Block::Ptr block, bool verify,
    std::function<void(bcos::Error::Ptr&&, bcos::protocol::BlockHeader::Ptr&&, bool _sysBlock)>
        _callback)
{
    if (block->blockHeader()->version() > (uint32_t)g_BCOSConfig.maxSupportedVersion())
    {
        auto errorMessage = "The block version is larger than maxSupportedVersion";
        SCHEDULER_LOG(WARNING) << errorMessage << LOG_KV("version", block->version())
                               << LOG_KV("maxSupportedVersion", g_BCOSConfig.maxSupportedVersion());
        _callback(std::make_shared<bcos::Error>(SchedulerError::InvalidBlockVersion, errorMessage),
            nullptr, false);
        return;
    }
    uint64_t waitT = 0;
    if (m_lastExecuteFinishTime > 0)
    {
        waitT = utcTime() - m_lastExecuteFinishTime;
    }
    if (waitT > 3000)
    {
        waitT = 0;
        m_lastExecuteFinishTime = 0;
    }
    auto signature = block->blockHeaderConst()->signatureList();
    try
    {
        fetchGasLimit(block->blockHeaderConst()->number());
    }
    catch (std::exception& e)
    {
        SCHEDULER_LOG(ERROR) << "fetchGasLimit exception: " << boost::diagnostic_information(e);
        _callback(BCOS_ERROR_WITH_PREV_PTR(
                      SchedulerError::fetchGasLimitError, "etchGasLimit exception", e),
            nullptr, false);
        return;
    }


    SCHEDULER_LOG(INFO) << METRIC << "ExecuteBlock request"
                        << LOG_KV("block number", block->blockHeaderConst()->number())
                        << LOG_KV("gasLimit", m_gasLimit) << LOG_KV("verify", verify)
                        << LOG_KV("signatureSize", signature.size())
                        << LOG_KV("tx count", block->transactionsSize())
                        << LOG_KV("meta tx count", block->transactionsMetaDataSize())
                        << LOG_KV("version", (bcos::protocol::Version)(block->version()))
                        << LOG_KV("waitT", waitT);

    auto callback = [_callback = std::move(_callback)](bcos::Error::Ptr&& error,
                        bcos::protocol::BlockHeader::Ptr&& blockHeader, bool _sysBlock) {
        SCHEDULER_LOG(INFO) << METRIC << "ExecuteBlock response"
                            << LOG_KV(error ? "error" : "ok", error ? error->what() : "ok");
        _callback(error == nullptr ? nullptr : std::move(error), std::move(blockHeader), _sysBlock);
    };

    auto executeLock =
        std::make_shared<std::unique_lock<std::mutex>>(m_executeMutex, std::try_to_lock);
    if (!executeLock->owns_lock())
    {
        auto message = "Another block is executing!";
        SCHEDULER_LOG(ERROR) << "ExecuteBlock error, " << message;
        callback(BCOS_ERROR_UNIQUE_PTR(SchedulerError::InvalidStatus, message), nullptr, false);
        return;
    }

    std::unique_lock<std::mutex> blocksLock(m_blocksMutex);
    // Note: if hit the cache, may return synced blockHeader with signatureList in some cases
    if (!m_blocks->empty())
    {
        auto requestNumber = block->blockHeaderConst()->number();
        auto& frontBlock = m_blocks->front();
        auto& backBlock = m_blocks->back();
        // Block already executed
        if (requestNumber >= frontBlock->number() && requestNumber <= backBlock->number())
        {
            SCHEDULER_LOG(INFO) << "ExecuteBlock success, return executed block"
                                << LOG_KV("block number", block->blockHeaderConst()->number())
                                << LOG_KV("signatureSize", signature.size())
                                << LOG_KV("verify", verify);

            auto it = m_blocks->begin();
            while (it->get()->number() != requestNumber)
            {
                ++it;
            }

            SCHEDULER_LOG(TRACE) << "BlockHeader stateRoot: " << std::hex
                                 << it->get()->result()->stateRoot();

            auto blockHeader = it->get()->result();

            blocksLock.unlock();
            executeLock->unlock();
            callback(nullptr, std::move(blockHeader), it->get()->sysBlock());
            return;
        }

        if (requestNumber - backBlock->number() != 1)
        {
            auto message =
                "Invalid block number: " +
                boost::lexical_cast<std::string>(block->blockHeaderConst()->number()) +
                " current last number: " + boost::lexical_cast<std::string>(backBlock->number());
            SCHEDULER_LOG(ERROR) << "ExecuteBlock error, " << message;

            blocksLock.unlock();
            executeLock->unlock();
            callback(BCOS_ERROR_PTR(SchedulerError::InvalidBlockNumber, std::move(message)),
                nullptr, false);

            return;
        }
    }
    else
    {
        std::promise<protocol::BlockNumber> blockNumberFuture;
        m_ledger->asyncGetBlockNumber(
            [&blockNumberFuture](Error::Ptr error, protocol::BlockNumber number) {
                if (error)
                {
                    SCHEDULER_LOG(ERROR) << "Scheduler get blockNumber from storage failed";
                    blockNumberFuture.set_value(-1);
                }
                else
                {
                    blockNumberFuture.set_value(number);
                }
            });


        auto currentBlockNumber = blockNumberFuture.get_future().get();

        if (currentBlockNumber != 0 &&
            currentBlockNumber + 1 != block->blockHeaderConst()->number())
        {
            auto message =
                (boost::format(
                     "Try to execute an discontinuous block: %ld, last current block number: %ld") %
                    block->blockHeaderConst()->number() % currentBlockNumber)
                    .str();
            SCHEDULER_LOG(ERROR) << "ExecuteBlock error, " << message;
            callback(
                BCOS_ERROR_UNIQUE_PTR(SchedulerError::InvalidBlockNumber, message), nullptr, false);
            return;
        }
    }

    BlockExecutive::Ptr blockExecutive = getPreparedBlock(
        block->blockHeaderConst()->number(), block->blockHeaderConst()->timestamp());

    if (blockExecutive == nullptr)
    {
        // the block has not been prepared, just make a new one here
        SCHEDULER_LOG(DEBUG) << LOG_BADGE("preExecuteBlock ")
                             << "Not hit prepared block executive, create."
                             << LOG_KV("block number", block->blockHeaderConst()->number());
        blockExecutive = std::make_shared<BlockExecutive>(std::move(block), this, 0,
            m_transactionSubmitResultFactory, false, m_blockFactory, m_txPool, m_gasLimit, verify);
    }
    else
    {
        SCHEDULER_LOG(DEBUG) << LOG_BADGE("preExecuteBlock ")
                             << "Hit prepared block executive cache, use it."
                             << LOG_KV("block number", block->blockHeaderConst()->number());
        blockExecutive->block()->setBlockHeader(block->blockHeader());
    }


    m_blocks->emplace_back(blockExecutive);

    blockExecutive = m_blocks->back();


    blocksLock.unlock();
    blockExecutive->asyncExecute([this, callback = std::move(callback), executeLock](
                                     Error::UniquePtr error, protocol::BlockHeader::Ptr header,
                                     bool _sysBlock) {
        if (!m_isRunning)
        {
            callback(BCOS_ERROR_UNIQUE_PTR(SchedulerError::Stopped, "Scheduler is not running"),
                nullptr, false);
            return;
        }

        if (error)
        {
            SCHEDULER_LOG(ERROR) << "Unknown error, " << boost::diagnostic_information(*error);
            {
                std::unique_lock<std::mutex> blocksLock(m_blocksMutex);
                m_blocks->pop_front();
            }
            executeLock->unlock();
            callback(
                BCOS_ERROR_WITH_PREV_PTR(SchedulerError::UnknownError, "Unknown error", *error),
                nullptr, _sysBlock);
            return;
        }
        auto signature = header->signatureList();
        SCHEDULER_LOG(INFO) << "ExecuteBlock success" << LOG_KV("block number", header->number())
                            << LOG_KV("hash", header->hash().abridged())
                            << LOG_KV("state root", header->stateRoot().hex())
                            << LOG_KV("receiptRoot", header->receiptsRoot().hex())
                            << LOG_KV("txsRoot", header->txsRoot().abridged())
                            << LOG_KV("gasUsed", header->gasUsed())
                            << LOG_KV("signatureSize", signature.size());

        m_lastExecuteFinishTime = utcTime();
        executeLock->unlock();
        callback(std::move(error), std::move(header), _sysBlock);
    });
}

void SchedulerImpl::commitBlock(bcos::protocol::BlockHeader::Ptr header,
    std::function<void(bcos::Error::Ptr&&, bcos::ledger::LedgerConfig::Ptr&&)> _callback)
{
    SCHEDULER_LOG(INFO) << "CommitBlock request" << LOG_KV("block number", header->number());

    auto callback = [_callback = std::move(_callback)](
                        bcos::Error::Ptr&& error, bcos::ledger::LedgerConfig::Ptr&& config) {
        SCHEDULER_LOG(INFO) << METRIC << "CommitBlock response"
                            << LOG_KV(error ? "error" : "ok", error ? error->what() : "ok");
        _callback(error == nullptr ? nullptr : std::move(error), std::move(config));
    };

    auto commitLock =
        std::make_shared<std::unique_lock<std::mutex>>(m_commitMutex, std::try_to_lock);
    if (!commitLock->owns_lock())
    {
        std::string message;
        {
            std::unique_lock<std::mutex> blocksLock(m_blocksMutex);
            if (m_blocks->empty())
            {
                message = (boost::format("commitBlock: empty block queue, maybe the block has been "
                                         "committed! Block number: %ld, hash: %s") %
                           header->number() % header->hash().abridged())
                              .str();
            }
            else
            {
                auto& frontBlock = m_blocks->front();
                message =
                    (boost::format(
                         "commitBlock: Another block is committing! Block number: %ld, hash: %s") %
                        frontBlock->block()->blockHeaderConst()->number() %
                        frontBlock->block()->blockHeaderConst()->hash().abridged())
                        .str();
            }
        }
        SCHEDULER_LOG(ERROR) << "CommitBlock error, " << message;
        callback(BCOS_ERROR_UNIQUE_PTR(SchedulerError::InvalidStatus, message), nullptr);
        return;
    }

    if (m_blocks->empty())
    {
        auto message = "No uncommitted block";
        SCHEDULER_LOG(ERROR) << "CommitBlock error, " << message;

        commitLock->unlock();
        callback(BCOS_ERROR_UNIQUE_PTR(SchedulerError::InvalidBlocks, message), nullptr);
        return;
    }

    auto& frontBlock = m_blocks->front();
    if (!frontBlock->result())
    {
        auto message = "Block is executing";
        SCHEDULER_LOG(ERROR) << "CommitBlock error, " << message;

        commitLock->unlock();
        callback(BCOS_ERROR_UNIQUE_PTR(SchedulerError::InvalidStatus, message), nullptr);
        return;
    }

    if (header->number() != frontBlock->number())
    {
        auto message = "Invalid block number, available block number: " +
                       boost::lexical_cast<std::string>(frontBlock->number());
        SCHEDULER_LOG(ERROR) << "CommitBlock error, " << message;

        commitLock->unlock();
        callback(BCOS_ERROR_UNIQUE_PTR(SchedulerError::InvalidBlockNumber, message), nullptr);
        return;
    }
    // Note: only when the signatureList is empty need to reset the header
    // in case of the signatureList of the header is accessing by the sync module while frontBlock
    // is setting newBlockHeader, which will cause the signatureList ilegal
    auto executedHeader = frontBlock->block()->blockHeader();
    auto signature = executedHeader->signatureList();
    if (signature.empty())
    {
        frontBlock->block()->setBlockHeader(std::move(header));
    }
    frontBlock->asyncCommit([this, callback = std::move(callback), block = frontBlock->block(),
                                commitLock](Error::UniquePtr&& error) {
        if (!m_isRunning)
        {
            callback(BCOS_ERROR_UNIQUE_PTR(SchedulerError::Stopped, "Scheduler is not running"),
                nullptr);
            return;
        }

        if (error)
        {
            SCHEDULER_LOG(ERROR) << "CommitBlock error, " << boost::diagnostic_information(*error);

            commitLock->unlock();
            callback(BCOS_ERROR_WITH_PREV_UNIQUE_PTR(
                         SchedulerError::UnknownError, "CommitBlock error", *error),
                nullptr);
            return;
        }

        asyncGetLedgerConfig([this, commitLock = std::move(commitLock),
                                 callback = std::move(callback)](
                                 Error::Ptr error, ledger::LedgerConfig::Ptr ledgerConfig) {
            if (!m_isRunning)
            {
                callback(BCOS_ERROR_UNIQUE_PTR(SchedulerError::Stopped, "Scheduler is not running"),
                    nullptr);
                return;
            }
            if (error)
            {
                SCHEDULER_LOG(ERROR)
                    << "Get system config error, " << boost::diagnostic_information(*error);

                commitLock->unlock();
                callback(BCOS_ERROR_WITH_PREV_UNIQUE_PTR(
                             SchedulerError::UnknownError, "Get system config error", *error),
                    nullptr);
                return;
            }

            auto& frontBlock = m_blocks->front();
            auto blockNumber = ledgerConfig->blockNumber();
            auto gasNumber = ledgerConfig->gasLimit();
            // Note: takes effect in next block. we query the enableNumber of blockNumber + 1.
            if (std::get<1>(gasNumber) <= (blockNumber + 1))
            {
                m_gasLimit = std::get<0>(gasNumber);
            }

            SCHEDULER_LOG(INFO) << "CommitBlock success" << LOG_KV("block number", blockNumber)
                                << LOG_KV("gas limit", m_gasLimit);

            // Note: block number = 0, means system deploy, and tx is not existed in txpool.
            // So it should not exec tx notifier
            if (m_txNotifier && blockNumber != 0)
            {
                SCHEDULER_LOG(INFO) << "Start notify block result: " << blockNumber;
                frontBlock->asyncNotify(m_txNotifier,
                    [this, blockNumber, callback = std::move(callback),
                        ledgerConfig = std::move(ledgerConfig),
                        commitLock = std::move(commitLock)](Error::Ptr _error) mutable {
                        if (!m_isRunning)
                        {
                            callback(BCOS_ERROR_UNIQUE_PTR(
                                         SchedulerError::Stopped, "Scheduler is not running"),
                                nullptr);
                            return;
                        }

                        if (m_blockNumberReceiver)
                        {
                            m_blockNumberReceiver(blockNumber);
                        }

                        SCHEDULER_LOG(INFO) << "End notify block result: " << blockNumber;

                        {
                            std::unique_lock<std::mutex> blocksLock(m_blocksMutex);
                            m_blocks->pop_front();
                            SCHEDULER_LOG(DEBUG)
                                << "Remove committed block: " << blockNumber << " success";
                        }

                        commitLock->unlock();
                        // Note: only after the block notify finished can call the callback
                        callback(std::move(_error), std::move(ledgerConfig));
                    });
            }
            else
            {
                {
                    std::unique_lock<std::mutex> blocksLock(m_blocksMutex);
                    bcos::protocol::BlockNumber number = m_blocks->front()->number();
                    removeAllOldPreparedBlock(number);
                    m_blocks->pop_front();
                    SCHEDULER_LOG(DEBUG) << "Remove committed block: " << blockNumber << " success";
                }

                commitLock->unlock();
                callback(nullptr, std::move(ledgerConfig));
            }
        });
    });
}

void SchedulerImpl::status(
    std::function<void(Error::Ptr&&, bcos::protocol::Session::ConstPtr&&)> callback)
{
    (void)callback;
}

void SchedulerImpl::call(protocol::Transaction::Ptr tx,
    std::function<void(Error::Ptr&&, protocol::TransactionReceipt::Ptr&&)> callback)
{
    // call but to is empty,
    // it will cause tx message be marked as 'create' falsely when asyncExecute tx
    if (tx->to().empty())
    {
        callback(BCOS_ERROR_PTR(SchedulerError::UnknownError, "Call address is empty"), nullptr);
        return;
    }
    // set attribute before call
    tx->setAttribute(m_isWasm ? bcos::protocol::Transaction::Attribute::LIQUID_SCALE_CODEC :
                                bcos::protocol::Transaction::Attribute::EVM_ABI_CODEC);
    // Create temp block
    auto block = m_blockFactory->createBlock();
    block->appendTransaction(std::move(tx));

    // Create temp executive
    auto blockExecutive =
        std::make_shared<BlockExecutive>(std::move(block), this, m_calledContextID.fetch_add(1),
            m_transactionSubmitResultFactory, true, m_blockFactory, m_txPool, m_gasLimit, false);

    blockExecutive->asyncCall([callback = std::move(callback)](Error::UniquePtr&& error,
                                  protocol::TransactionReceipt::Ptr&& receipt) {
        if (error)
        {
            SCHEDULER_LOG(ERROR) << "Unknown error, " << boost::diagnostic_information(*error);
            callback(
                BCOS_ERROR_WITH_PREV_PTR(SchedulerError::UnknownError, "Unknown error", *error),
                nullptr);
            return;
        }
        SCHEDULER_LOG(INFO) << "Call success";
        callback(nullptr, std::move(receipt));
    });
}

void SchedulerImpl::registerExecutor(std::string name,
    bcos::executor::ParallelTransactionExecutorInterface::Ptr executor,
    std::function<void(Error::Ptr&&)> callback)
{
    try
    {
        SCHEDULER_LOG(INFO) << "registerExecutor request: " << LOG_KV("name", name);
        m_executorManager->addExecutor(name, executor);
    }
    catch (std::exception& e)
    {
        SCHEDULER_LOG(ERROR) << "registerExecutor error: " << boost::diagnostic_information(e);
        callback(BCOS_ERROR_WITH_PREV_PTR(-1, "addExecutor error", e));
        return;
    }

    SCHEDULER_LOG(INFO) << "registerExecutor success";
    callback(nullptr);
}

void SchedulerImpl::unregisterExecutor(
    const std::string& name, std::function<void(Error::Ptr&&)> callback)
{
    (void)name;
    (void)callback;
}

void SchedulerImpl::reset(std::function<void(Error::Ptr&&)> callback)
{
    (void)callback;
}
// register a block number receiver
void SchedulerImpl::registerBlockNumberReceiver(
    std::function<void(protocol::BlockNumber blockNumber)> callback)
{
    m_blockNumberReceiver = [callback = std::move(callback)](
                                protocol::BlockNumber blockNumber) { callback(blockNumber); };
}

void SchedulerImpl::getCode(
    std::string_view contract, std::function<void(Error::Ptr, bcos::bytes)> callback)
{
    auto executor = m_executorManager->dispatchExecutor(contract);
    executor->getCode(contract, std::move(callback));
}

void SchedulerImpl::getABI(
    std::string_view contract, std::function<void(Error::Ptr, std::string)> callback)
{
    auto executor = m_executorManager->dispatchExecutor(contract);
    executor->getABI(contract, std::move(callback));
}

void SchedulerImpl::registerTransactionNotifier(std::function<void(bcos::protocol::BlockNumber,
        bcos::protocol::TransactionSubmitResultsPtr, std::function<void(Error::Ptr)>)>
        txNotifier)
{
    m_txNotifier = [callback = std::move(txNotifier)](bcos::protocol::BlockNumber blockNumber,
                       bcos::protocol::TransactionSubmitResultsPtr resultsPtr,
                       std::function<void(Error::Ptr)> _callback) {
        callback(blockNumber, resultsPtr, std::move(_callback));
    };
}

BlockExecutive::Ptr SchedulerImpl::getPreparedBlock(
    bcos::protocol::BlockNumber blockNumber, int64_t timestamp)
{
    bcos::ReadGuard readGuard(x_preparedBlockMutex);

    if (m_preparedBlocks.count(blockNumber) != 0 &&
        m_preparedBlocks[blockNumber].count(timestamp) != 0)
    {
        return m_preparedBlocks[blockNumber][timestamp];
    }
    else
    {
        return nullptr;
    }
}

void SchedulerImpl::setPreparedBlock(
    bcos::protocol::BlockNumber blockNumber, int64_t timestamp, BlockExecutive::Ptr blockExecutive)
{
    bcos::WriteGuard writeGuard(x_preparedBlockMutex);

    m_preparedBlocks[blockNumber][timestamp] = blockExecutive;
}

void SchedulerImpl::removeAllOldPreparedBlock(bcos::protocol::BlockNumber oldBlockNumber)
{
    bcos::WriteGuard writeGuard(x_preparedBlockMutex);

    // erase all preparedBlock <= oldBlockNumber
    for (auto itr = m_preparedBlocks.begin(); itr != m_preparedBlocks.end();)
    {
        if (itr->first <= oldBlockNumber)
        {
            SCHEDULER_LOG(DEBUG) << LOG_BADGE("prepareBlockExecutive")
                                 << LOG_DESC("removeAllOldPreparedBlock")
                                 << LOG_KV("block number", itr->first);
            itr = m_preparedBlocks.erase(itr);
        }
        else
        {
            itr++;
        }
    }
}

void SchedulerImpl::preExecuteBlock(
    bcos::protocol::Block::Ptr block, bool verify, std::function<void(Error::Ptr&&)> _callback)
{
    auto startT = utcTime();
    SCHEDULER_LOG(INFO) << "preExecuteBlock request"
                        << LOG_KV("block number", block->blockHeaderConst()->number())
                        << LOG_KV("tx count",
                               block->transactionsSize() + block->transactionsMetaDataSize())
                        << LOG_KV("startT(ms)", startT);

    auto callback = [startT, _callback = std::move(_callback)](bcos::Error::Ptr&& error) {
        SCHEDULER_LOG(INFO) << METRIC << "preExecuteBlock response"
                            << LOG_KV(error ? "error" : "ok", error ? error->what() : "ok")
                            << LOG_KV("cost(ms)", utcTime() - startT);
        _callback(error == nullptr ? nullptr : std::move(error));
    };

    auto blockNumber = block->blockHeaderConst()->number();
    int64_t timestamp = block->blockHeaderConst()->timestamp();
    BlockExecutive::Ptr blockExecutive = getPreparedBlock(blockNumber, timestamp);
    if (blockExecutive != nullptr)
    {
        SCHEDULER_LOG(DEBUG) << LOG_BADGE("prepareBlockExecutive")
                             << "Duplicate block to prepare, dropped."
                             << LOG_KV("blockHeader.timestamp", timestamp);
        callback(nullptr);  // also success
    }

    blockExecutive = std::make_shared<BlockExecutive>(std::move(block), this, 0,
        m_transactionSubmitResultFactory, false, m_blockFactory, m_txPool, m_gasLimit, verify);

    setPreparedBlock(blockNumber, timestamp, blockExecutive);

    blockExecutive->prepare();

    callback(nullptr);
}

template <class... Ts>
struct overloaded : Ts...
{
    using Ts::operator()...;
};
// explicit deduction guide (not needed as of C++20)
template <class... Ts>
overloaded(Ts...) -> overloaded<Ts...>;

void SchedulerImpl::asyncGetLedgerConfig(
    std::function<void(Error::Ptr, ledger::LedgerConfig::Ptr ledgerConfig)> callback)
{
    auto ledgerConfig = std::make_shared<ledger::LedgerConfig>();
    auto callbackPtr = std::make_shared<decltype(callback)>(std::move(callback));
    auto summary =
        std::make_shared<std::tuple<size_t, std::atomic_size_t, std::atomic_size_t>>(8, 0, 0);

    auto collector = [this, summary = std::move(summary), ledgerConfig = std::move(ledgerConfig),
                         callback = std::move(callbackPtr)](Error::Ptr error,
                         std::variant<std::tuple<bool, consensus::ConsensusNodeListPtr>,
                             std::tuple<int, std::string, bcos::protocol::BlockNumber>,
                             bcos::protocol::BlockNumber, bcos::crypto::HashType>&&
                             result) mutable {
        if (!m_isRunning)
        {
            (*callback)(BCOS_ERROR_UNIQUE_PTR(SchedulerError::Stopped, "Scheduler is not running"),
                nullptr);
            return;
        }

        auto& [total, success, failed] = *summary;

        if (error)
        {
            SCHEDULER_LOG(ERROR) << "Get ledger config with errors: "
                                 << boost::diagnostic_information(*error);
            ++failed;
        }
        else
        {
            std::visit(
                overloaded{
                    [&ledgerConfig](std::tuple<bool, consensus::ConsensusNodeListPtr>& nodeList) {
                        auto& [isSealer, list] = nodeList;

                        if (isSealer)
                        {
                            ledgerConfig->setConsensusNodeList(*list);
                        }
                        else
                        {
                            ledgerConfig->setObserverNodeList(*list);
                        }
                    },
                    [&ledgerConfig](std::tuple<int, std::string, protocol::BlockNumber> config) {
                        auto& [type, value, blockNumber] = config;
                        switch (type)
                        {
                        case 0:
                            ledgerConfig->setBlockTxCountLimit(
                                boost::lexical_cast<uint64_t>(value));
                            break;
                        case 1:
                            ledgerConfig->setLeaderSwitchPeriod(
                                boost::lexical_cast<uint64_t>(value));
                            break;
                        case 2:
                            ledgerConfig->setGasLimit(
                                std::make_tuple(boost::lexical_cast<uint64_t>(value), blockNumber));
                            break;
                        case 3:
                            try
                            {
                                auto version = bcos::tool::toVersionNumber(value);
                                ledgerConfig->setCompatibilityVersion(version);
                            }
                            catch (std::exception const& e)
                            {
                                SCHEDULER_LOG(WARNING) << LOG_DESC("invalidVersionNumber") << value;
                            }
                            break;
                        default:
                            BOOST_THROW_EXCEPTION(BCOS_ERROR(SchedulerError::UnknownError,
                                "Unknown type: " + boost::lexical_cast<std::string>(type)));
                        }
                    },
                    [&ledgerConfig](bcos::protocol::BlockNumber number) {
                        ledgerConfig->setBlockNumber(number);
                    },
                    [&ledgerConfig](bcos::crypto::HashType hash) { ledgerConfig->setHash(hash); }},
                result);

            ++success;
        }

        // Collect done
        if (success + failed == total)
        {
            if (failed > 0)
            {
                SCHEDULER_LOG(ERROR) << "Get ledger config with error: " << failed;
                (*callback)(
                    BCOS_ERROR_PTR(SchedulerError::UnknownError, "Get ledger config with error"),
                    nullptr);

                return;
            }

            (*callback)(nullptr, std::move(ledgerConfig));
        }
    };

    m_ledger->asyncGetNodeListByType(ledger::CONSENSUS_SEALER,
        [collector](Error::Ptr error, consensus::ConsensusNodeListPtr list) mutable {
            collector(std::move(error), std::tuple{true, std::move(list)});
        });
    m_ledger->asyncGetNodeListByType(ledger::CONSENSUS_OBSERVER,
        [collector](Error::Ptr error, consensus::ConsensusNodeListPtr list) mutable {
            collector(std::move(error), std::tuple{false, std::move(list)});
        });
    m_ledger->asyncGetSystemConfigByKey(ledger::SYSTEM_KEY_TX_COUNT_LIMIT,
        [collector](Error::Ptr error, std::string config, protocol::BlockNumber _number) mutable {
            collector(std::move(error), std::tuple{0, std::move(config), _number});
        });
    m_ledger->asyncGetSystemConfigByKey(ledger::SYSTEM_KEY_CONSENSUS_LEADER_PERIOD,
        [collector](Error::Ptr error, std::string config, protocol::BlockNumber _number) mutable {
            collector(std::move(error), std::tuple{1, std::move(config), _number});
        });
    m_ledger->asyncGetSystemConfigByKey(ledger::SYSTEM_KEY_TX_GAS_LIMIT,
        [collector](Error::Ptr error, std::string config, protocol::BlockNumber _number) mutable {
            collector(std::move(error), std::tuple{2, std::move(config), _number});
        });
    m_ledger->asyncGetBlockNumber(
        [collector, ledger = m_ledger](Error::Ptr error, protocol::BlockNumber number) mutable {
            ledger->asyncGetBlockHashByNumber(
                number, [collector](Error::Ptr error, const crypto::HashType& hash) mutable {
                    collector(std::move(error), std::move(hash));
                });
            collector(std::move(error), std::move(number));
        });

    // Note: The consensus module ensures serial execution and submit of system txs
    m_ledger->asyncGetSystemConfigByKey(ledger::SYSTEM_KEY_COMPATIBILITY_VERSION,
        [collector](Error::Ptr error, std::string config, protocol::BlockNumber _number) mutable {
            collector(std::move(error), std::tuple{3, std::move(config), _number});
        });
}