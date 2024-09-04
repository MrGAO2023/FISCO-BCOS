package org.example.finance.service;


import org.example.finance.model.Result;
import org.example.finance.model.bo.LoginBO;
import org.example.finance.model.bo.RegisterBO;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Arthur
 * @since 2021-09-07
 */
public interface IOrgService {
    Result<String> login(@RequestBody LoginBO loginBO);
    Result<String> register(RegisterBO registerBO);
}
