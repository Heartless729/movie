package lab.io.rush.movie.cache.redis;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

import lab.io.rush.movie.cache.redis.mapper.CustomerObjectMapper;

@Configuration
@EnableCaching
public class RedisConfig extends CachingConfigurerSupport{
	@Bean
	public KeyGenerator wiselyKeyGenerator(){
		return new KeyGenerator(){

			@Override
			public Object generate(Object target, Method method, Object... params) {
				// TODO Auto-generated method stub
				StringBuilder sb = new StringBuilder();
				sb.append(target.getClass().getName());
				sb.append(method.getName());
				for(Object obj:params){
					sb.append(obj.toString());
				}
				return sb.toString();
			}
			
		};
	}
	@Bean
	public CacheManager cacheManager(RedisTemplate redisTemplate){
		RedisCacheManager cacheManager = new RedisCacheManager(redisTemplate);
		cacheManager.setDefaultExpiration(600);//设置key-value超时时间
		Map<String,Long> map = new HashMap<>();
		map.put("MovieTicketNum", 3L);//设置余票缓存3秒
		cacheManager.setExpires(map);
		return cacheManager;
	}
	@Bean
	public RedisTemplate<String,String> redisTemplate(RedisConnectionFactory factory)throws Throwable{
		StringRedisTemplate template=new StringRedisTemplate(factory);
		setSerializer(template);//设置序列化工具，这样ReportBean不需要实现Serializable接口
		template.afterPropertiesSet();
		return template;
	}
	private void setSerializer(StringRedisTemplate template){
		Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
		CustomerObjectMapper om = new CustomerObjectMapper();
		om.setVisibility(PropertyAccessor.ALL,JsonAutoDetect.Visibility.ANY);
		om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		jackson2JsonRedisSerializer.setObjectMapper(om);
		template.setValueSerializer(jackson2JsonRedisSerializer);
	}
}
