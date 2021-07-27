/*
 * MIT License
 *
 * Copyright (c) 2021 OpeningO Co.,Ltd.
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

package org.openingo.boot.mybatisplus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.openingo.jdkits.lang.BeanKit;
import org.openingo.jdkits.reflect.ClassKit;
import org.openingo.jdkits.validate.ValidateKit;

import java.util.Collections;
import java.util.List;

/**
 * IBaseService
 *
 * @author Qicz
 * @since 2021/7/26 15:35
 */
public interface IBaseService<VO, DO> extends IService<DO>  {

	/**
	 * put之前的工作
	 * @param vo 某对象
	 */
	default void beforePut(VO vo) {
	}

	/**
	 * 创建或编辑一个对象
	 * @param vo 某对象
	 * @return true成功false失败
	 */
	default boolean put(VO vo) {
		beforePut(vo);
		Class<DO> doClass = ClassKit.getGenericType(this, 1);
		final DO aDo = BeanKit.copy(vo, doClass);
		boolean ret = this.saveOrUpdate(aDo);
		afterPut(vo, aDo);
		return ret;
	}

	/**
	 * put之后的工作
	 * @param vo 某对象vo
	 * @param aDo 某对象do
	 */
	default void afterPut(VO vo, DO aDo) {

	}

	/**
	 * 转换为DO
	 * @param vo 某实体
	 * @return 实体对应的DO
	 */
	default DO toDO(VO vo) {
		Class<DO> doClass = ClassKit.getGenericType(this, 1);
		return BeanKit.copy(vo, doClass);
	}

	/**
	 * 转换为VO
	 * @param aDo 某实体
	 * @return 实体对应的DO
	 */
	default VO toVO(DO aDo) {
		Class<VO> voClass = ClassKit.getGenericType(this, 0);
		return BeanKit.copy(aDo, voClass);
	}

	/**
	 * 所有对象
	 * @return 所有对象集合
	 */
	default List<VO> listAll() {
		final List<DO> list = this.list();
		if (ValidateKit.isEmpty(list)) {
			return Collections.emptyList();
		}
		Class<VO> voClass = ClassKit.getGenericType(this, 0);
		return BeanKit.copy(list, voClass);
	}
}
