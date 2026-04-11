package com.mall.platform.controller;

import com.mall.platform.auth.AuthBinding;
import com.mall.platform.common.Result;
import com.mall.platform.dto.UserAddressSaveDTO;
import com.mall.platform.service.UserAddressService;
import com.mall.platform.vo.UserAddressVO;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户收货地址接口。
 */
@Validated
@RestController
@RequestMapping("/api/user/addresses")
public class UserAddressController {

    private final UserAddressService userAddressService;

    public UserAddressController(UserAddressService userAddressService) {
        this.userAddressService = userAddressService;
    }

    /**
     * 我的地址列表。
     */
    @GetMapping
    public Result<List<UserAddressVO>> list() {
        return Result.success(userAddressService.listByUserId(AuthBinding.currentUserIdOrThrow()));
    }

    /**
     * 默认地址；无默认返回 null。
     */
    @GetMapping("/default")
    public Result<UserAddressVO> getDefault() {
        return Result.success(userAddressService.getDefault(AuthBinding.currentUserIdOrThrow()));
    }

    /**
     * 新建地址。
     */
    @PostMapping
    public Result<UserAddressVO> create(@Valid @RequestBody UserAddressSaveDTO dto) {
        long uid = AuthBinding.currentUserIdOrThrow();
        AuthBinding.assertSameUser(dto.getUserId(), uid);
        dto.setUserId(uid);
        return Result.success(userAddressService.create(dto));
    }

    /**
     * 修改地址。
     */
    @PutMapping("/{id}")
    public Result<UserAddressVO> update(@PathVariable("id") Long id, @Valid @RequestBody UserAddressSaveDTO dto) {
        long uid = AuthBinding.currentUserIdOrThrow();
        AuthBinding.assertSameUser(dto.getUserId(), uid);
        dto.setUserId(uid);
        return Result.success(userAddressService.update(id, dto));
    }

    /**
     * 删除地址（软删）。
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable("id") Long id) {
        long uid = AuthBinding.currentUserIdOrThrow();
        userAddressService.delete(id, uid);
        return Result.success("删除成功", "OK");
    }

    /**
     * 设为默认地址。
     */
    @PostMapping("/{id}/default")
    public Result<UserAddressVO> setAsDefault(@PathVariable("id") Long id) {
        long uid = AuthBinding.currentUserIdOrThrow();
        return Result.success(userAddressService.setAsDefault(id, uid));
    }
}
