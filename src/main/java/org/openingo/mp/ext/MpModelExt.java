/*
 * MIT License
 *
 * Copyright (c) 2020 OpeningO Co.,Ltd.
 *
 *    https://openingo.org
 *    contactus(at)openingo.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.openingo.mp.ext;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.AbstractLambdaWrapper;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import org.openingo.jdkits.JacksonKit;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * MyBatis Plus Extensions: ModelExt
 *
 * @author Qicz
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class MpModelExt<T extends MpModelExt<?>> extends Model<T> {

    @TableField(exist = false)
    private final T typedThis = (T) this;

    @TableField(exist = false)
    private static final String LIMIT_ONE = "LIMIT 1";

    @TableField(exist = false)
    private Map<String, AbstractLambdaWrapper> wrappersMapping;

    /**
     * 生成实例
     * @param clazz
     * @param <T>
     */
    private static<T extends MpModelExt<?>> T getInstance(Class<T> clazz) {
        T t = null;
        try {
            t = clazz.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            throw new MybatisPlusException(e.getLocalizedMessage());
        }
        return t;
    }

    /**
     * 获取dao对象，每次生成一个新的
     * @param clazz
     * @param <T>
     * @return T
     */
    public static<T extends MpModelExt<?>> T dao(Class<T> clazz) {
        return getInstance(clazz);
    }

    /**
     * 初始化WrappersMapping
     */
    private void initWrappersMapping() {
        if (null == this.wrappersMapping) {
            this.wrappersMapping = new HashMap<>();
            this.wrappersMapping.put("q", new LambdaQueryWrapper<T>());
            this.wrappersMapping.put("u", new LambdaUpdateWrapper<T>());
        }
    }

    /**
     * 获取Wrappers
     */
    private List<AbstractLambdaWrapper> getWrappers() {
        this.initWrappersMapping();
        return new ArrayList<>(this.wrappersMapping.values());
    }

    /**
     * 获取QueryWrapper，如果为null，创建一个新的
     */
    private LambdaQueryWrapper<T> getQueryWrapper() {
        this.initWrappersMapping();
        return (LambdaQueryWrapper<T>) this.wrappersMapping.get("q");
    }

    /**
     * 获取UpdateWrapper，如果为null，创建一个新的
     */
    private LambdaUpdateWrapper<T> getUpdateWrapper() {
        this.initWrappersMapping();
        return (LambdaUpdateWrapper<T>) this.wrappersMapping.get("u");
    }

    /**
     * 执行Query操作
     * @return List<T>
     */
    public List<T> doQuery() {
        return this.selectList(this.getQueryWrapper());
    }

    /**
     * 执行Select One Limit 1 Query操作
     * @return T
     */
    public T doQueryLimitOne() {
        // clone 不影响原对象，解决添加last之后，再做其他的操作
        return this.selectOne(this.getQueryWrapper().clone().last(LIMIT_ONE));
    }

    /**
     * 执行Select One Query操作
     * @return T
     */
    public T doQueryOne() {
        return this.selectOne(this.getQueryWrapper());
    }

    /**
     * 执行Update操作
     * @return true成功，false失败
     */
    public Boolean doUpdate() {
        return this.update(this.getUpdateWrapper());
    }

    /**
     * 执行Delete操作
     * @return true成功，false失败
     */
    public Boolean doDelete() {
        return this.delete(this.getUpdateWrapper());
    }

    /**
     * 基于t进行操作
     * @param t
     * @return T
     */
    public T by(T t) {
        this.getWrappers().forEach(wrapper -> wrapper.setEntity(t));
        return typedThis;
    }

    /**
     * SELECT 部分 SQL 设置
     *
     * @param columns 查询字段
     */
    @SafeVarargs
    public final T select(SFunction<T, ?>... columns) {
        this.getQueryWrapper().select(columns);
        return typedThis;
    }

    /**
     * 过滤查询的字段信息(主键除外!)
     * <p>例1: 只要 java 字段名以 "test" 开头的             -> select(i -> i.getProperty().startsWith("test"))</p>
     * <p>例2: 只要 java 字段属性是 CharSequence 类型的     -> select(TableFieldInfo::isCharSequence)</p>
     * <p>例3: 只要 java 字段没有填充策略的                 -> select(i -> i.getFieldFill() == FieldFill.DEFAULT)</p>
     * <p>例4: 要全部字段                                   -> select(i -> true)</p>
     * <p>例5: 只要主键字段                                 -> select(i -> false)</p>
     *
     * @param predicate 过滤方式
     * @return T
     */
    public T select(Predicate<TableFieldInfo> predicate) {
        return this.select((Class<T>)this.getClass(), predicate);
    }

    /**
     * 过滤查询的字段信息(主键除外!)
     * <p>例1: 只要 java 字段名以 "test" 开头的             -> select(i -> i.getProperty().startsWith("test"))</p>
     * <p>例2: 只要 java 字段属性是 CharSequence 类型的     -> select(TableFieldInfo::isCharSequence)</p>
     * <p>例3: 只要 java 字段没有填充策略的                 -> select(i -> i.getFieldFill() == FieldFill.DEFAULT)</p>
     * <p>例4: 要全部字段                                   -> select(i -> true)</p>
     * <p>例5: 只要主键字段                                 -> select(i -> false)</p>
     *
     * @param predicate 过滤方式
     * @return T
     */
    public T select(Class<T> entityClass, Predicate<TableFieldInfo> predicate) {
        this.getQueryWrapper().select(entityClass, predicate);
        return typedThis;
    }

    /**
     * map 所有非空属性等于 =
     *
     * @param params      map 类型的参数, key 是字段名, value 是字段值
     * @return T
     */
    public <V> T allEq(Map<SFunction<T, ?>, V> params) {
        return this.allEq(params, true);
    }

    /**
     * map 所有非空属性等于 =
     *
     * @param params      map 类型的参数, key 是字段名, value 是字段值
     * @param null2IsNull 是否参数为 null 自动执行 isNull 方法, false 则忽略这个字段\
     * @return T
     */
    public <V> T allEq(Map<SFunction<T, ?>, V> params, boolean null2IsNull) {
        return this.allEq(true, params, true);
    }

    /**
     * 字段过滤接口，传入多参数时允许对参数进行过滤
     *
     * @param filter      返回 true 来允许字段传入比对条件中
     * @param params      map 类型的参数, key 是字段名, value 是字段值
     * @return T
     */
    public <V> T allEq(BiPredicate<SFunction<T, ?>, V> filter, Map<SFunction<T, ?>, V> params) {
        return this.allEq(filter, params, true);
    }

    /**
     * 字段过滤接口，传入多参数时允许对参数进行过滤
     *
     * @param filter      返回 true 来允许字段传入比对条件中
     * @param params      map 类型的参数, key 是字段名, value 是字段值
     * @param null2IsNull 是否参数为 null 自动执行 isNull 方法, false 则忽略这个字段
     * @return T
     */
    public <V> T allEq(BiPredicate<SFunction<T, ?>, V> filter, Map<SFunction<T, ?>, V> params, boolean null2IsNull) {
        this.getWrappers().forEach(wrapper -> wrapper.allEq(filter, params, null2IsNull));
        return typedThis;
    }

    /**
     * map 所有非空属性等于 =
     *
     * @param condition   执行条件
     * @param params      map 类型的参数, key 是字段名, value 是字段值
     * @param null2IsNull 是否参数为 null 自动执行 isNull 方法, false 则忽略这个字段\
     * @return T
     */
    public <V> T allEq(boolean condition, Map<SFunction<T, ?>, V> params, boolean null2IsNull) {
        this.getWrappers().forEach(wrapper -> wrapper.allEq(condition, params, null2IsNull));
        return typedThis;
    }

    /**
     * 字段过滤接口，传入多参数时允许对参数进行过滤
     *
     * @param condition   执行条件
     * @param filter      返回 true 来允许字段传入比对条件中
     * @param params      map 类型的参数, key 是字段名, value 是字段值
     * @param null2IsNull 是否参数为 null 自动执行 isNull 方法, false 则忽略这个字段
     * @return T
     */
    public <V> T allEq(boolean condition, BiPredicate<SFunction<T, ?>, V> filter, Map<SFunction<T, ?>, V> params, boolean null2IsNull) {
        this.getWrappers().forEach(wrapper -> wrapper.allEq(condition, filter, params, null2IsNull));
        return typedThis;
    }

    /**
     * 等于 =
     *
     * @param column    字段
     * @param val       值
     * @return T
     */
    public T eq(SFunction<T, ?> column, Object val) {
        return this.eq(true, column, val);
    }

    /**
     * 等于 =
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val       值
     * @return T
     */
    public T eq(boolean condition, SFunction<T, ?> column, Object val) {
        this.getWrappers().forEach(wrapper -> wrapper.eq(condition, column, val));
        return typedThis;
    }

    /**
     * 不等于 &lt;&gt;
     *
     * @param column    字段
     * @param val       值
     * @return T
     */
    public T ne(SFunction<T, ?> column, Object val) {
        return this.ne(true, column, val);
    }

    /**
     * 不等于 &lt;&gt;
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val       值
     * @return T
     */
    public T ne(boolean condition, SFunction<T, ?> column, Object val) {
        this.getWrappers().forEach(wrapper -> wrapper.ne(condition, column, val));
        return typedThis;
    }

    /**
     * 大于 &gt;
     *
     * @param column    字段
     * @param val       值
     * @return T
     */
    public T gt(SFunction<T, ?> column, Object val) {
        return this.gt(true, column, val);
    }

    /**
     * 大于 &gt;
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val       值
     * @return T
     */
    public T gt(boolean condition, SFunction<T, ?> column, Object val) {
        this.getWrappers().forEach(wrapper -> wrapper.gt(condition, column, val));
        return typedThis;
    }

    /**
     * 大于等于 &gt;=
     *
     * @param column    字段
     * @param val       值
     * @return children
     */
    public T ge(SFunction<T, ?> column, Object val) {
        return this.ge(true, column, val);
    }

    /**
     * 大于等于 &gt;=
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val       值
     * @return T
     */
    public T ge(boolean condition, SFunction<T, ?> column, Object val) {
        this.getWrappers().forEach(wrapper -> wrapper.ge(condition, column, val));
        return typedThis;
    }

    /**
     * 小于 &lt;
     *
     * @param column    字段
     * @param val       值
     * @return T
     */
    public T lt(SFunction<T, ?> column, Object val) {
        return this.lt(true, column, val);
    }

    /**
     * 小于 &lt;
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val       值
     * @return T
     */
    public T lt(boolean condition, SFunction<T, ?> column, Object val) {
        this.getWrappers().forEach(wrapper -> wrapper.lt(condition, column, val));
        return typedThis;
    }

    /**
     * 小于等于 &lt;=
     *
     * @param column    字段
     * @param val       值
     * @return T
     */
    public T le(SFunction<T, ?> column, Object val) {
        return this.le(true, column, val);
    }

    /**
     * 小于等于 &lt;=
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val       值
     * @return T
     */
    public T le(boolean condition, SFunction<T, ?> column, Object val) {
        this.getWrappers().forEach(wrapper -> wrapper.le(condition, column, val));
        return typedThis;
    }

    /**
     * BETWEEN 值1 AND 值2
     *
     * @param column    字段
     * @param val1      值1
     * @param val2      值2
     * @return T
     */
    public T between(SFunction<T, ?> column, Object val1, Object val2) {
        return this.between(true, column, val1, val2);
    }

    /**
     * BETWEEN 值1 AND 值2
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val1      值1
     * @param val2      值2
     * @return T
     */
    public T between(boolean condition, SFunction<T, ?> column, Object val1, Object val2) {
        this.getWrappers().forEach(wrapper -> wrapper.between(condition, column, val1, val2));
        return typedThis;
    }

    /**
     * NOT BETWEEN 值1 AND 值2
     *
     * @param column    字段
     * @param val1      值1
     * @param val2      值2
     * @return T
     */
    public T notBetween(SFunction<T, ?> column, Object val1, Object val2) {
        return this.notBetween(true, column, val1, val2);
    }

    /**
     * NOT BETWEEN 值1 AND 值2
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val1      值1
     * @param val2      值2
     * @return T
     */
    public T notBetween(boolean condition, SFunction<T, ?> column, Object val1, Object val2) {
        this.getWrappers().forEach(wrapper -> wrapper.notBetween(condition, column, val1, val2));
        return typedThis;
    }

    /**
     * LIKE '%值%'
     *
     * @param column    字段
     * @param val       值
     * @return T
     */
    public T like(SFunction<T, ?> column, Object val) {
        return this.like(true, column, val);
    }

    /**
     * LIKE '%值%'
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val       值
     * @return T
     */
    public T like(boolean condition, SFunction<T, ?> column, Object val) {
        this.getWrappers().forEach(wrapper -> wrapper.like(condition, column, val));
        return typedThis;
    }

    /**
     * NOT LIKE '%值%'
     *
     * @param column    字段
     * @param val       值
     * @return T
     */
    public T notLike(SFunction<T, ?> column, Object val) {
        return this.notLike(true, column, val);
    }

    /**
     * NOT LIKE '%值%'
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val       值
     * @return T
     */
    public T notLike(boolean condition, SFunction<T, ?> column, Object val) {
        this.getWrappers().forEach(wrapper -> wrapper.notLike(condition, column, val));
        return typedThis;
    }

    /**
     * LIKE '%值'
     *
     * @param column    字段
     * @param val       值
     * @return T
     */
    public T likeLeft(SFunction<T, ?> column, Object val) {
        return this.likeLeft(true, column, val);
    }

    /**
     * LIKE '%值'
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val       值
     * @return T
     */
    public T likeLeft(boolean condition, SFunction<T, ?> column, Object val) {
        this.getWrappers().forEach(wrapper -> wrapper.likeLeft(condition, column, val));
        return typedThis;
    }

    /**
     * LIKE '值%'
     *
     * @param column    字段
     * @param val       值
     * @return T
     */
    public T likeRight(SFunction<T, ?> column, Object val) {
        return this.likeRight(true, column, val);
    }

    /**
     * LIKE '值%'
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val       值
     * @return T
     */
    public T likeRight(boolean condition, SFunction<T, ?> column, Object val) {
        this.getWrappers().forEach(wrapper -> wrapper.likeRight(condition, column, val));
        return typedThis;
    }

    /**
     * 拼接 OR
     *
     * @return T
     */
    public T or() {
        return this.or(true);
    }

    /**
     * 拼接 OR
     *
     * @param condition 执行条件
     * @return T
     */
    public T or(boolean condition) {
        this.getWrappers().forEach(wrapper -> wrapper.or(condition));
        return typedThis;
    }

    /**
     * OR 嵌套
     * <p>
     * 例: or(i -&gt; i.eq("name", "李白").ne("status", "活着"))
     * </p>
     *
     * @param consumer  消费函数
     * @return T
     */
    public <Children extends AbstractWrapper<T, SFunction<T, ?>, Children>> T or(Consumer<Children> consumer) {
        return this.or(true, consumer);
    }

    /**
     * OR 嵌套
     * <p>
     * 例: or(i -&gt; i.eq("name", "李白").ne("status", "活着"))
     * </p>
     *
     * @param condition 执行条件
     * @param consumer  消费函数
     * @return T
     */
    public <Children extends AbstractWrapper<T, SFunction<T, ?>, Children>> T or(boolean condition, Consumer<Children> consumer) {
        this.getWrappers().forEach(wrapper -> wrapper.or(condition, consumer));
        return typedThis;
    }

    /**
     * AND 嵌套
     * <p>
     * 例: and(i -&gt; i.eq("name", "李白").ne("status", "活着"))
     * </p>
     *
     * @param consumer  消费函数
     * @return T
     */
    public <Children extends AbstractWrapper<T, SFunction<T, ?>, Children>> T and(Consumer<Children> consumer) {
        return this.and(true, consumer);
    }

    /**
     * AND 嵌套
     * <p>
     * 例: and(i -&gt; i.eq("name", "李白").ne("status", "活着"))
     * </p>
     *
     * @param condition 执行条件
     * @param consumer  消费函数
     * @return T
     */
    public <Children extends AbstractWrapper<T, SFunction<T, ?>, Children>> T and(boolean condition, Consumer<Children> consumer) {
        this.getWrappers().forEach(wrapper -> wrapper.and(condition, consumer));
        return typedThis;
    }

    /**
     * 正常嵌套 不带 AND 或者 OR
     * <p>
     * 例: nested(i -&gt; i.eq("name", "李白").ne("status", "活着"))
     * </p>
     *
     * @param consumer  消费函数
     * @return T
     */
    public <Children extends AbstractWrapper<T, SFunction<T, ?>, Children>> T nested(Consumer<Children> consumer) {
        return this.nested(true, consumer);
    }

    /**
     * 正常嵌套 不带 AND 或者 OR
     * <p>
     * 例: nested(i -&gt; i.eq("name", "李白").ne("status", "活着"))
     * </p>
     *
     * @param condition 执行条件
     * @param consumer  消费函数
     * @return T
     */
    public <Children extends AbstractWrapper<T, SFunction<T, ?>, Children>> T nested(boolean condition, Consumer<Children> consumer) {
        this.getWrappers().forEach(wrapper -> wrapper.nested(condition, consumer));
        return typedThis;
    }

    /**
     * 设置 更新 SQL 的 SET 片段
     *
     * @param column    字段
     * @param val       值
     * @return T
     */
    public T set(SFunction<T, ?> column, Object val) {
        return this.set(true, column, val);
    }

    /**
     * 设置 更新 SQL 的 SET 片段
     *
     * @param condition 是否加入 set
     * @param column    字段
     * @param val       值
     * @return T
     */
    public T set(boolean condition, SFunction<T, ?> column, Object val) {
        this.getUpdateWrapper().set(condition, column, val);
        return typedThis;
    }

    /**
     * 字段 IS NULL
     * <p>例: isNull("name")</p>
     *
     * @param column    字段
     * @return T
     */
    public T isNull(SFunction<T, ?> column) {
        return this.isNull(true, column);
    }

    /**
     * 字段 IS NULL
     * <p>例: isNull("name")</p>
     *
     * @param condition 执行条件
     * @param column    字段
     * @return T
     */
    public T isNull(boolean condition, SFunction<T, ?> column) {
        this.getWrappers().forEach(wrapper -> wrapper.isNull(condition, column));
        return typedThis;
    }

    /**
     * 字段 IS NOT NULL
     * <p>例: isNotNull("name")</p>
     *
     * @param column    字段
     * @return T
     */
    public T isNotNull(SFunction<T, ?> column) {
        return this.isNotNull(true, column);
    }

    /**
     * 字段 IS NOT NULL
     * <p>例: isNotNull("name")</p>
     *
     * @param condition 执行条件
     * @param column    字段
     * @return T
     */
    public T isNotNull(boolean condition, SFunction<T, ?> column) {
        this.getWrappers().forEach(wrapper -> wrapper.isNotNull(condition, column));
        return typedThis;
    }

    /**
     * 字段 IN (value.get(0), value.get(1), ...)
     * <p>例: in("id", Arrays.asList(1, 2, 3, 4, 5))</p>
     *
     * <li> 如果集合为 empty 则不会进行 sql 拼接 </li>
     *
     * @param column    字段
     * @param coll      数据集合
     * @return T
     */
    public T in(SFunction<T, ?> column, Collection<?> coll) {
        return this.in(true, column, coll);
    }

    /**
     * 字段 IN (value.get(0), value.get(1), ...)
     * <p>例: in("id", Arrays.asList(1, 2, 3, 4, 5))</p>
     *
     * <li> 如果集合为 empty 则不会进行 sql 拼接 </li>
     *
     * @param condition 执行条件
     * @param column    字段
     * @param coll      数据集合
     * @return T
     */
    public T in(boolean condition, SFunction<T, ?> column, Collection<?> coll) {
        this.getWrappers().forEach(wrapper -> wrapper.in(condition, column, coll));
        return typedThis;
    }

    /**
     * 字段 NOT IN (value.get(0), value.get(1), ...)
     * <p>例: notIn("id", Arrays.asList(1, 2, 3, 4, 5))</p>
     *
     * @param column    字段
     * @param coll      数据集合
     * @return T
     */
    public T notIn(SFunction<T, ?> column, Collection<?> coll) {
        return this.notIn(true, column, coll);
    }

    /**
     * 字段 NOT IN (value.get(0), value.get(1), ...)
     * <p>例: notIn("id", Arrays.asList(1, 2, 3, 4, 5))</p>
     *
     * @param condition 执行条件
     * @param column    字段
     * @param coll      数据集合
     * @return T
     */
    public T notIn(boolean condition, SFunction<T, ?> column, Collection<?> coll) {
        this.getWrappers().forEach(wrapper -> wrapper.notIn(condition, column, coll));
        return typedThis;
    }

    /**
     * 分组：GROUP BY 字段, ...
     * <p>例: groupBy("id", "name")</p>
     *
     * @param columns   字段数组
     * @return T
     */
    @SafeVarargs
    public final T groupBy(SFunction<T, ?>... columns) {
        return this.groupBy(true, columns);
    }

    /**
     * 分组：GROUP BY 字段, ...
     * <p>例: groupBy("id", "name")</p>
     *
     * @param condition 执行条件
     * @param columns   字段数组
     * @return T
     */
    @SafeVarargs
    public final T groupBy(boolean condition, SFunction<T, ?>... columns) {
        this.getWrappers().forEach(wrapper -> wrapper.groupBy(condition, columns));
        return typedThis;
    }

    /**
     * 排序：ORDER BY 字段, ...
     * <p>例: orderBy(true, "id", "name")</p>
     *
     * @param isAsc     是否是 ASC 排序
     * @param columns   字段数组
     * @return T
     */
    @SafeVarargs
    public final T orderBy(boolean isAsc, SFunction<T, ?>... columns) {
        return this.orderBy(true, isAsc, columns);
    }

    /**
     * 排序：ORDER BY 字段, ...
     * <p>例: orderBy(true, "id", "name")</p>
     *
     * @param condition 执行条件
     * @param isAsc     是否是 ASC 排序
     * @param columns   字段数组
     * @return T
     */
    @SafeVarargs
    public final T orderBy(boolean condition, boolean isAsc, SFunction<T, ?>... columns) {
        this.getWrappers().forEach(wrapper -> wrapper.orderBy(condition, isAsc, columns));
        return typedThis;
    }

    /**
     * 排序：ORDER BY 字段, ... ASC
     * <p>例: orderByAsc("id", "name")</p>
     *
     * @param columns   字段数组
     * @return T
     */
    @SafeVarargs
    public final T orderByAsc(SFunction<T, ?>... columns) {
        return this.orderByAsc(true, columns);
    }

    /**
     * 排序：ORDER BY 字段, ... ASC
     * <p>例: orderByAsc("id", "name")</p>
     *
     * @param condition 执行条件
     * @param columns   字段数组
     * @return T
     */
    @SafeVarargs
    public final T orderByAsc(boolean condition, SFunction<T, ?>... columns) {
        this.getWrappers().forEach(wrapper -> wrapper.orderByAsc(condition, columns));
        return typedThis;
    }

    /**
     * 排序：ORDER BY 字段, ... DESC
     * <p>例: orderByDesc("id", "name")</p>
     *
     * @param columns   字段数组
     * @return T
     */
    @SafeVarargs
    public final T orderByDesc(SFunction<T, ?>... columns) {
        return this.orderByDesc(true, columns);
    }

    /**
     * 排序：ORDER BY 字段, ... DESC
     * <p>例: orderByDesc("id", "name")</p>
     *
     * @param condition 执行条件
     * @param columns   字段数组
     * @return T
     */
    @SafeVarargs
    public final T orderByDesc(boolean condition, SFunction<T, ?>... columns) {
        this.getWrappers().forEach(wrapper -> wrapper.orderByDesc(condition, columns));
        return typedThis;
    }

    /**
     * 消费函数
     *
     * @param condition
     * @param consumer  消费函数
     * @return T
     * @since Mp3.3.1
     */
    public <Children extends AbstractWrapper<T, SFunction<T, ?>, Children>> T func(boolean condition, Consumer<Children> consumer) {
        this.getWrappers().forEach(wrapper -> wrapper.func(condition, consumer));
        return typedThis;
    }

    // ====== other logic ====

    /**
     * 转换为JsonString
     */
    public String toJson() {
        return JacksonKit.toJson(this);
    }
}
