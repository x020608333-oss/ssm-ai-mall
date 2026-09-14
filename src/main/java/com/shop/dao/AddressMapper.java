package com.shop.dao;

import com.shop.entity.Address;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AddressMapper {

    int insert(Address address);

    List<Address> findByUser(@Param("userId") Long userId);

    Address findByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);

    int deleteByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);

    int clearDefault(@Param("userId") Long userId);

    int setDefault(@Param("id") Long id, @Param("userId") Long userId);

    int countByUser(@Param("userId") Long userId);
}
