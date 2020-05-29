/** 
 * @Project Name : LuckyExp
*
* @File name : Two.java
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
package org.lucky.test;

import java.io.Serializable;

import org.lucky.exp.annotation.BindVar;
import org.lucky.exp.annotation.Calculation;

public class Two implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@BindVar("E")
	private Double age;
	@BindVar("F")
	private Double clazz;
	@BindVar("G")
	@Calculation(formula= {"A+B*D","M * roundUp(max(A,2,3,4)/2.1)"})
	private Double sex;
	//@BindDouble(key="K")
	//@Calculation(formula= {"A+B*D","M * roundUp(max(A,2,3,4)/2.1)"})
	private String hello;
	@BindVar("WW")
	@Calculation(formula= {"(A+1+O)*100","M * roundUp(max(A,2,3,4)/2.1)"})
	private Double y;
	@BindVar("EE")
	@Calculation(formula= {"(A+1)*100","M * roundUp(max(A,2,3,4)/2.1)"})
	private Double o;
	@BindVar("RR")
	@Calculation(formula= {"(A+B*D)*100","M * roundUp(max(A,2,3,4)/2.1)*100"})
	private Double c;
	public Double getAge() {
		return age;
	}
	public void setAge(Double age) {
		this.age = age;
	}
	public Double getClazz() {
		return clazz;
	}
	public void setClazz(Double clazz) {
		this.clazz = clazz;
	}
	public Double getSex() {
		return sex < 1000 ? 520:sex;
		//return sex;
	}
	public void setSex(Double sex) {
		this.sex = sex;
	}
	public String getHello() {
		return hello;
	}
	public void setHello(String hello) {
		this.hello = hello;
	}
	public Double getY() {
		return y;
	}
	public void setY(Double y) {
		this.y = y;
	}
	public Double getO() {
		return o;
	}
	public void setO(Double o) {
		this.o = o;
	}
	public Double getC() {
		return c;
	}
	public void setC(Double c) {
		this.c = c;
	}
	
}
