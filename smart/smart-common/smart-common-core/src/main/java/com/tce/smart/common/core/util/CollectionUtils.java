package com.tce.smart.common.core.util;

import com.google.common.collect.Sets;
import com.tce.smart.common.core.constant.NumberConstants;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName CollectionUtil
 * @Author: WangJinbo
 * @Date: 2018/10/26 14:32
 * @Description:
 **/
public class CollectionUtils extends org.springframework.util.CollectionUtils {
    /**
     * 数组是否为空
     *
     * @param array 数组
     * @return 是否为空
     */
    public static <T> boolean isEmpty(T[] array) {
        return array == null || array.length == NumberConstants.ZERO;
    }

    /**
     * 数组是否为非空
     *
     * @param array 数组
     * @return 是否为非空
     */
    public static <T> boolean isNotEmpty(T[] array) {
        return !isEmpty(array);
    }

    /**
     * 集合是否为非空
     *
     * @param collection 集合
     * @return 是否为非空
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    /**
     * map是否为非空
     *
     * @param map
     * @return 是否为非空
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    /**
     * 获取第一个元素
     *
     * @param list
     * @return 第一个元素
     */
    public static <T> T getFirst(List<T> list) {
        return isEmpty(list) ? null : list.get(NumberConstants.ZERO);
    }

    /**
     * 获取第一个元素
     *
     * @param array
     * @return 第一个元素
     */
    public static <T> T getFirst(T[] array) {
        return isEmpty(array) ? null : array[NumberConstants.ZERO];
    }

    /**
     * 差集
     *
     * @param from
     * @param target
     * @param <T>
     * @param <F>
     * @return
     */
    public static <T, F> Map<T, F> difference(Map<T, F> from, Map<T, F> target) {
        return difference(from.keySet(), target.keySet()).stream().collect(Collectors.toMap(key -> key, from::get));
    }

    /**
     * 差集
     * 根据key，从from取value
     *
     * @param from
     * @param target
     * @param <T>
     * @return
     */
    public static <T> Set<T> difference(Set<T> from, Set<T> target) {
        return Sets.difference(from, target);
    }

    /**
     * 差集
     * 根据key，从from取value
     *
     * @param from
     * @param target
     * @param <T>
     * @return
     */
    public static <T> Set<T> difference(List<T> from, List<T> target) {
        return Sets.difference(Sets.newHashSet(from), Sets.newHashSet(target));
    }

    /**
     * 交集
     * 根据key，从from取value
     *
     * @param from
     * @param target
     * @param <T>
     * @return
     */
    public static <T, F> Map<T, F> intersection(Map<T, F> from, Map<T, F> target) {
        return intersection(from.keySet(), target.keySet()).stream().collect(Collectors.toMap(key -> key, from::get));
    }

    /**
     * 交集
     *
     * @param from
     * @param target
     * @param <T>
     * @return
     */
    public static <T> Set<T> intersection(Set<T> from, Set<T> target) {
        return Sets.intersection(from, target);
    }

    /**
     * 并集
     * 1、如果from存在key，则根据key，从from取value
     * 2、如果from不存在key，target存在key，则根据key，从target取value
     *
     * @param from
     * @param target
     * @param <T>
     * @return
     */
    public static <T, F> Map<T, F> union(Map<T, F> from, Map<T, F> target) {
        return union(from.keySet(), target.keySet()).stream()
                .collect(Collectors.toMap(key -> key, key -> from.containsKey(key) ? from.get(key) : target.get(key)));
    }

    /**
     * 并集
     *
     * @param from
     * @param target
     * @param <T>
     * @return
     */
    public static <T> Set<T> union(Set<T> from, Set<T> target) {
        return Sets.union(from, target);
    }
}
