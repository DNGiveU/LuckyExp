package org.lucky.exp;
import static org.lucky.exp.missYaner.MissYaner.convertToRPN;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import org.lucky.exp.annotation.BindVar;
import org.lucky.exp.annotation.BindType;
import org.lucky.exp.annotation.Condition;
import org.lucky.exp.cache.Cache;
import org.lucky.exp.cache.CacheToken;
import org.lucky.exp.exception.LuckyExpEvaluateException;
import org.lucky.exp.tokenizer.FunctionToken;
import org.lucky.exp.tokenizer.NumberToken;
import org.lucky.exp.tokenizer.OperToken;
import org.lucky.exp.tokenizer.Token;
import org.lucky.exp.tokenizer.VariableToken;
import org.lucky.exp.util.Iterator;
import org.lucky.exp.util.LinkedStack;
/**
 * 计算结果处理，
 * 目的是在组装回对象时，把计算的结果给组装进来
 * @author FayeWong
 * @since 1.0
 *
 */
public class HandlerResult {
	/**
	 *  将计算出来的结果通过get方法组装到对象中
	 * @param configuration 配置类
	 * @param result 计算结果
	 * @param exp 被拆分的对象
	 * @throws LuckyExpEvaluateException 回调异常
	 */
	private static  void setBean(Configuration configuration, double result, Map<Condition, Object> exp)
			throws LuckyExpEvaluateException {
		Field field = (Field) exp.get(Condition.field);
		BindVar bind = field.getAnnotation(BindVar.class);
		String value = new DecimalFormat(exp.get(Condition.format).toString()).format(result);
		try {
			try {
				if(field.getType() == BindType.f.getType() || field.getType() == BindType.F.getType()) {
					field.set(exp.get(Condition.entity), Float.valueOf(value));			
				}else if(field.getType() == BindType.d.getType() || field.getType() == BindType.D.getType()) {
					field.set(exp.get(Condition.entity), Double.valueOf(value));			
				}else if(field.getType() == BindType.s.getType() || field.getType() == BindType.S.getType()) {
					field.set(exp.get(Condition.entity), Short.valueOf(value));				
				}else if(field.getType() == BindType.i.getType() || field.getType() == BindType.I.getType()) {
					field.set(exp.get(Condition.entity), Integer.valueOf(value));				
				}else if(field.getType() == BindType.l.getType() || field.getType() == BindType.L.getType()) {
					field.set(exp.get(Condition.entity), Long.valueOf(value));		
				}else if(field.getType() == BindType.STR.getType()) {
					field.set(exp.get(Condition.entity), value);			
				}
			}catch (NumberFormatException e) {
				throw new LuckyExpEvaluateException("变量 ' "+field.getName()+" ',类型 ' "+field.getType()+"' ,结果 ' "+value+" ' 转换赋值失败",e);
			}
			if (bind != null) {
				// 从get中获取结果变量，get方法逻辑返回的值给下一个结果计算
				//PropertyDescriptor pd = new PropertyDescriptor(field.getName(), exp.get(Condition.entity).getClass());
				//Method getMethod = pd.getReadMethod();
				//Object getResult = getMethod.invoke((Object) exp.get(Condition.entity));
				configuration.getVariables().put(bind.value(), result);
				configuration.getVariableNames().addAll(configuration.getVariables().keySet());
			}
		} catch (IllegalArgumentException e) {
			throw new LuckyExpEvaluateException(e);
		} catch (IllegalAccessException e) {
			throw new LuckyExpEvaluateException(e);
		} 
	}

	/**
	 * 从栈堆推送结果
	 * @param configuration 配置对象
	 * @param tokensMap Tokens
	 * @param iterator 公式对象
	 * @param throwable 回调结果不抛找不到变量异常
	 * @throws LuckyExpEvaluateException 回调异常
	 */
	private static  void evaluate(Configuration configuration, Map<String, Token[]> tokensMap,
			Iterator<Map<Condition, Object>> iterator,boolean throwable) throws LuckyExpEvaluateException {		
		while (iterator.hasNext()) {
			LinkedStack<Object> output = new LinkedStack<Object>();
			Map<Condition, Object> exp = iterator.removeNext();
			String expressionKey = (String) exp.get(Condition.expression);
			Token[] tokens = tokensMap.get(expressionKey);
			Field field = (Field) exp.get(Condition.field);
			for (int i = 0; i < tokens.length; i++) {				
				Token t = tokens[i];
				if (t.getType() == Token.TOKEN_NUMBER) {
					output.push(((NumberToken) t).getValue());
				} else if (t.getType() == Token.TOKEN_VARIABLE) {
					final String name = ((VariableToken) t).getName();
					final Double value = configuration.getVariables().get(name);
					try {
						if (value == null) {
							configuration.addErrors("' 公式 "+expressionKey+" ' 变量 ' "+field.getName()+" ',参数 ' "+name+" ' 为空\r\n");
							iterator.offerLast(exp);
							evaluate(configuration, tokensMap, iterator,throwable);
							return;
						} else {
							output.push(value);
						}
					}catch (StackOverflowError error) {
						iterator.offerLast(exp);
						if(throwable) {
							throw new LuckyExpEvaluateException("重新计算失败，有未知参数 ' "+" ' "+configuration.getErrors());
						}
						return;
					}	
				} else if (t.getType() == Token.TOKEN_OPERATOR) {
					OperToken op = (OperToken) t;
					if (output.size() < op.getOper().getNumOperands()) {
						throw new LuckyExpEvaluateException("变量 '" + field.getName() + "',可用于的操作数无效 '"
								+ op.getOper().getSymbol() + "' oper(操作数只接受1或2)");
					}
					if (op.getOper().getNumOperands() == 2) {
						/* 弹出操作数并推送操作结果 */
						double rightArg = (double)output.pop();
						double leftArg = (double)output.pop();
						output.push(op.getOper().call(leftArg, rightArg));
					} else if (op.getOper().getNumOperands() == 1) {
						/* 弹出操作数并推送操作结果 */
						double arg = (double)output.pop();
						output.push(op.getOper().call(arg));
					}
				} else if (t.getType() == Token.TOKEN_FUNCTION) {
					FunctionToken func = (FunctionToken) t;
					final int numArguments = func.getFunction().getNumArguments();
					if (output.size() < numArguments) {
						throw new LuckyExpEvaluateException("变量' " + field.getName() + " ',无可用于计算的参数 '"
								+ func.getFunction().getName() + "' function");
					}
					/* 从堆栈收集参数 */
					Object[] args = new Object[numArguments];
					for (int j = numArguments - 1; j >= 0; j--) {
						args[j] = (Object)output.pop();
					}
					output.push(func.getFunction().call(args));
				}
			}
			if (output.size() > 1) {
				throw new LuckyExpEvaluateException("变量 ' " + field.getName() + " ',输出队列中的参数无效。可能是函数的参数无法解析导致的.");
			} 
			setBean(configuration, (double)output.pop(), exp);
		};
	}
	/**
	 * 从表达式中推送出来的结果组装到各个对象中
	 * @param configuration 配置对象
	 * @param cacheToken 缓存对象
	 * @param throwable 回调结果不抛找不到变量异常
	 * @return 是否计算成
	 * @throws LuckyExpEvaluateException 回调异常
	 */
	public static boolean evaluateObject(Configuration configuration,CacheToken cacheToken,boolean throwable) throws LuckyExpEvaluateException {
		final Iterator<Map<Condition, Object>> iterator = new Iterator<Map<Condition, Object>>(configuration.getPassExps());
		final Map<String,Token[]> tokensMap = new HashMap<String,Token[]>();
		while (iterator.hasNext()) {
			Map<Condition, Object> exp = iterator.next();
			Field field = (Field) exp.get(Condition.field);
			final String expression = (String) exp.get(Condition.expression);
			cacheToken  = Cache.getInstance().builder((cToken)->{
				if(cToken.openCache()) {
					Token[] cacheTokens = cToken.getToken(expression);
					if(cacheTokens == null) {
						cacheTokens = convertToRPN(expression, field, configuration);
						cToken.putTokens(expression, cacheTokens,cToken.expire());
					}
				}else {
					Token[] tokens = convertToRPN(expression, field, configuration);
					tokensMap.put(expression, tokens);
				}
			});			
		};
		iterator.reset();
		evaluate(configuration, cacheToken.openCache() ? cacheToken.getTokensMap() : tokensMap, iterator,throwable);
		if(iterator.isEmpty()) {
			configuration.getErrors().clear();
			return true;
		}
		return false;
	}
}
