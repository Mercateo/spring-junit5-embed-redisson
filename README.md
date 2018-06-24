A tiny wrapper for conveniently using [embedded Redis Server]() with JUnit5 and Spring Boot.

Mainly this is an excuse to play with JUnit5's ParameterResolver interface. 

In order to create a Spring Boot Test, you need to use JUnit5.


First add 
```			<dependency>
			<groupId>test</groupId>
			<artifactId>junit5-redis</artifactId>
			<version>[0,)</version> <!-- or a particular version -->
			<scope>test</scope>
		</dependency>``` 
		
to your pom.xml and create a SpringBoot Redisson Test like this:

```
@SpringBootRedissonTest // provides the extensions
public class RedissonTest {
	
	@Test
	public void testWithInjectedRedissonParameter(RedissonClient c) {

		RDeque<Object> deque = c.getDeque("mylist");
		assertEquals(0, deque.size());
		
		deque.add("hubba");
		assertEquals("hubba", deque.pop());
	}

	@Test
	public void testWithoutRedissonParameter() {
		// there is no redis server being started for this test, as there is no redisson parameter
	}
}

``` 


