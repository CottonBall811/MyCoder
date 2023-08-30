package com.mycoder.community.dao;

import com.mycoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {

    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);

    // @Param give an alias to the parameters
    // If there is only one param, and used in <if>, then alias is required.
    int selectDiscussPostRows(@Param("userId") int userId);

}
