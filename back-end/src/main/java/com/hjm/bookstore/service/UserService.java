package com.hjm.bookstore.service;

import com.hjm.bookstore.entity.UserAddress;
import com.hjm.bookstore.entity.UserInfo;
import com.hjm.bookstore.repository.UserAddressRepository;
import com.hjm.bookstore.repository.UserInfoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 用户服务类
 */
@Service
@Slf4j
public class UserService {

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private UserAddressRepository userAddressRepository;

    /**
     * 获取用户信息
     */
    public Optional<UserInfo> getUserInfo(Integer userId) {
        return userInfoRepository.findById(userId);
    }

    /**
     * 更新用户信息
     */
    @Transactional
    public UserInfo updateUserInfo(Integer userId, String username, String phone) {
        Optional<UserInfo> userOpt = userInfoRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }

        UserInfo user = userOpt.get();
        if (username != null && !username.isEmpty()) {
            user.setUsername(username);
        }
        if (phone != null && !phone.isEmpty()) {
            // 检查手机号是否已被其他用户使用
            Optional<UserInfo> existingUser = userInfoRepository.findByPhone(phone);
            if (existingUser.isPresent() && !existingUser.get().getUserId().equals(userId)) {
                throw new RuntimeException("该手机号已被使用");
            }
            user.setPhone(phone);
        }

        return userInfoRepository.save(user);
    }

    /**
     * 获取用户地址列表
     */
    public List<UserAddress> getUserAddresses(Integer userId) {
        return userAddressRepository.findByUserId(userId);
    }

    /**
     * 添加用户地址
     */
    @Transactional
    public UserAddress addAddress(Integer userId, String address, Integer isDefault) {
        // 如果设置为默认地址，先取消其他默认地址
        if (isDefault == 1) {
            List<UserAddress> addresses = userAddressRepository.findByUserId(userId);
            for (UserAddress addr : addresses) {
                if (addr.getIsDefault() == 1) {
                    addr.setIsDefault(0);
                    userAddressRepository.save(addr);
                }
            }
        }

        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(userId);
        userAddress.setAddress(address);
        userAddress.setIsDefault(isDefault);

        return userAddressRepository.save(userAddress);
    }

    /**
     * 更新用户地址
     */
    @Transactional
    public UserAddress updateAddress(Integer addressId, String address, Integer isDefault) {
        Optional<UserAddress> addrOpt = userAddressRepository.findById(addressId);
        if (addrOpt.isEmpty()) {
            throw new RuntimeException("地址不存在");
        }

        UserAddress userAddress = addrOpt.get();

        // 如果设置为默认地址，先取消其他默认地址
        if (isDefault == 1) {
            List<UserAddress> addresses = userAddressRepository.findByUserId(userAddress.getUserId());
            for (UserAddress addr : addresses) {
                if (addr.getIsDefault() == 1 && !addr.getAddressId().equals(addressId)) {
                    addr.setIsDefault(0);
                    userAddressRepository.save(addr);
                }
            }
        }

        if (address != null && !address.isEmpty()) {
            userAddress.setAddress(address);
        }
        if (isDefault != null) {
            userAddress.setIsDefault(isDefault);
        }

        return userAddressRepository.save(userAddress);
    }

    /**
     * 删除用户地址
     */
    @Transactional
    public void deleteAddress(Integer addressId) {
        userAddressRepository.deleteById(addressId);
        log.info("删除地址: {}", addressId);
    }
}
