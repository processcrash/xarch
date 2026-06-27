package com.xarch.example.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Thin wrapper around Spring's {@link BeanUtils#copyProperties} that logs
 * copy failures and provides collection-aware variants.
 *
 * <p>Why wrap?
 * <ul>
 *   <li>Spring's copy is silent on partial mismatches — making it hard to
 *       trace a missing field to the wrong copy site.</li>
 *   <li>We need a single place to enforce consistent behaviour across
 *       micro-services.</li>
 * </ul>
 */
public final class BeanCopyUtil {

    private static final Logger log = LoggerFactory.getLogger(BeanCopyUtil.class);

    private BeanCopyUtil() {
    }

    /**
     * Copy properties from {@code source} into a <em>new</em> instance of
     * {@code targetClass}.
     *
     * @param source       source object (may be {@code null})
     * @param targetClass  destination class; must have a no-arg constructor
     * @param <T>          destination type
     * @return populated target instance, or {@code null} if {@code source} is {@code null}
     */
    public static <T> T copy(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(source, target);
            return target;
        } catch (ReflectiveOperationException e) {
            log.error("Bean copy failed: source={}, target={}", source.getClass(), targetClass, e);
            return null;
        }
    }

    /**
     * Copy properties from {@code source} into an <em>existing</em>
     * {@code target} instance.
     *
     * @param source  source object (may be {@code null})
     * @param target  destination instance to populate
     */
    public static void copy(Object source, Object target) {
        if (source == null || target == null) {
            return;
        }
        try {
            BeanUtils.copyProperties(source, target);
        } catch (Exception e) {
            log.error("Bean copy failed: source={}, target={}", source.getClass(), target.getClass(), e);
        }
    }

    /**
     * Copy each element of {@code sources} into a new instance of
     * {@code targetClass}. Null entries are skipped.
     *
     * @param sources      list of source objects (may be {@code null})
     * @param targetClass  destination class; must have a no-arg constructor
     * @param <S>          source type
     * @param <T>          destination type
     * @return list of populated target instances (never {@code null})
     */
    public static <S, T> List<T> copyList(List<S> sources, Class<T> targetClass) {
        List<T> result = new ArrayList<>();
        if (sources == null) {
            return result;
        }
        for (S source : sources) {
            T copy = copy(source, targetClass);
            if (copy != null) {
                result.add(copy);
            }
        }
        return result;
    }
}