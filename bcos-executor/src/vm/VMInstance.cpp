/*
 *  Copyright (C) 2021 FISCO BCOS.
 *  SPDX-License-Identifier: Apache-2.0
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * @brief c++ wrapper of vm
 * @file VMInstance.cpp
 * @author: xingqiangbai
 * @date: 2021-05-24
 */

#include "VMInstance.h"
#include "HostContext.h"
#include "evmone/advanced_analysis.hpp"
#include "evmone/advanced_execution.hpp"

using namespace std;
namespace bcos::executor
{

VMInstance::VMInstance(evmc_vm* instance, evmc_revision revision, bytes_view code) noexcept
  : m_instance(instance), m_revision(revision), m_code(code)
{
    assert(m_instance != nullptr);
    // the abi_version of intepreter is EVMC_ABI_VERSION when callback VMFactory::create()
    assert(m_instance->abi_version == EVMC_ABI_VERSION);

    // Set the options.
    if (m_instance->set_option)
    {  // baseline interpreter could not work with precompiled
        m_instance->set_option(m_instance, "advanced", "");
        // m_instance->set_option(m_instance, "trace", "");
    }
}

VMInstance::VMInstance(
    std::shared_ptr<evmoneCodeAnalysis> analysis, evmc_revision revision, bytes_view code) noexcept
  : m_analysis(analysis), m_revision(revision), m_code(code)
{
    assert(m_analysis != nullptr);
}

Result VMInstance::execute(HostContext& _hostContext, evmc_message* _msg)
{
    if (m_instance)
    {
        return Result(m_instance->execute(m_instance, _hostContext.interface, &_hostContext,
            m_revision, _msg, m_code.data(), m_code.size()));
    }
    auto state = std::make_unique<evmone::advanced::AdvancedExecutionState>(
        *_msg, m_revision, *_hostContext.interface, &_hostContext, m_code);
    {  // baseline

        // auto vm = evmc_create_evmone();
        // return Result(evmone::baseline::execute(*static_cast<evmone::VM*>(vm), *state,
        // *m_analysis));
    }
    // advanced, TODO: state also could be reused
    return Result(evmone::advanced::execute(*state, *m_analysis));
}

evmc_revision toRevision(VMSchedule const& _schedule)
{
    if (_schedule.enablePairs)
        return EVMC_PARIS;
    else if (_schedule.enableLondon)
        return EVMC_LONDON;
    return EVMC_FRONTIER;
}
}  // namespace bcos::executor
