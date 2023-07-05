package com.whale.framework.common.utils;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @Description: bean操作工具
 * @Author Whale
 * @Date: 2023/6/27 10:12 AM
 */
@Slf4j
public class BeanUtil {

    public static <E, T> T copy(E e, Class<? extends T> clazz) {
        AssertUtil.assertNotNull(e, "source can not be null!");

        T t;
        try {
            t = clazz.newInstance();
            org.springframework.beans.BeanUtils.copyProperties(e, t);
        } catch (Exception e1) {
            return null;
        }
        return t;
    }

    public static <E, T> List<T> copyList(List<E> e, Class<? extends T> clazz) {

        if (CollectionUtils.isEmpty(e)) {
            return Lists.newArrayList();
        }

        List<T> list = new ArrayList<>();
        for (E object : e) {
            list.add(copy(object, clazz));
        }
        return list;
    }


    /**
     * 获取对象列表中所有对象的属性集合
     *
     * @param beanList 对象列表
     * @param field    属性名
     * @param distinct 是否去重
     * @param <T>      返回的属性类型
     * @return
     */
    public static <T> List<T> getBeanProperty(List beanList, String field, boolean distinct) {
        List<T> newList = new ArrayList();
        if (beanList == null || beanList.isEmpty()) {
            return newList;
        }
        beanList.stream().forEach((bean) -> {
            try {
                Map map = org.apache.commons.beanutils.BeanUtils.describe(bean);
                if (map.get(field) != null) {
                    newList.add((T) map.get(field));
                }
            } catch (Exception e) {
                LoggerUtils.error(log, "unexpected exception on bean describe operation!");
            }
        });
        if (distinct) {
            return new ArrayList(new HashSet(newList));
        }
        return newList;
    }

}