/** 
 * @Project Name : LuckyExp
*
* @File name : UnknownFunctionOrVariableException.java
*
* @Author : FayeWong
*
* @Email : 50125289@qq.com
*
----------------------------------------------------------------------------------
*    Who        Version     Comments
* 1. FayeWong    1.0
*
*
*
*
----------------------------------------------------------------------------------
*/
package org.lucky.exp.exception;

import java.lang.reflect.Field;

import org.lucky.exp.tokenizer.Tokenizer;

/**
 * 
* 每当{@link Tokenizer}发现未知的函数或变量时，就会引发此异常。
* @author FayeWong
* 
 */
public class UnknownFunOrVarException extends IllegalArgumentException {

	private static final long serialVersionUID = 1L;

	private final String message;
	private final String expression;
	private final String token;
	private final int position;

	public UnknownFunOrVarException(Field field,String expression, int position, int length) {
		this.expression = expression;
		this.token = token(expression, position, length);
		this.position = position;
		this.message = "变量 '"+field.getName()+"，'未知函数或参量 '" + token + "'，位置 '" + position + "'，不在表达式: '" + expression + "'中";
	}

	private static String token(String expression, int position, int length) {

		int len = expression.length();
		int end = position + length - 1;

		if (len < end) {
			end = len;
		}

		return expression.substring(position, end);
	}

	@Override
	public String getMessage() {
		return message;
	}

	/**
	 * @return 包含未知函数或变量的表达式
	 */
	public String getExpression() {
		return expression;
	}

	/**
	 * @return 未知函数或变量的名称
	 */
	public String getToken() {
		return token;
	}

	/**
	 * @return 未知函数或变量的位置
	 */
	public int getPosition() {
		return position;
	}
}
