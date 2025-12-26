package ulitsa.raskolnikova.cache;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

@Interceptor
@LogCacheStatistics
@Dependent
public class CacheStatisticsInterceptor {

    @Inject
    private CacheStatisticsService cacheStatisticsService;

    @AroundInvoke
    public Object logCacheStatistics(InvocationContext context) throws Exception {
        System.out.println("[CacheStatisticsInterceptor] Interceptor called for method: " + 
            context.getMethod().getDeclaringClass().getSimpleName() + "." + context.getMethod().getName());
        
        LogCacheStatistics annotation = getAnnotation(context);
        
        if (annotation == null) {
            System.out.println("[CacheStatisticsInterceptor] No annotation found");
            return context.proceed();
        }
        
        if (!annotation.enabled()) {
            System.out.println("[CacheStatisticsInterceptor] Annotation disabled");
            return context.proceed();
        }
        
        if (cacheStatisticsService == null) {
            System.out.println("[CacheStatisticsInterceptor] WARNING: cacheStatisticsService is null");
            return context.proceed();
        }
        
        if (!cacheStatisticsService.isLoggingEnabled()) {
            System.out.println("[CacheStatisticsInterceptor] Logging disabled in service");
            return context.proceed();
        }

        String methodName = context.getMethod().getDeclaringClass().getSimpleName() + "." + context.getMethod().getName();
        System.out.println("[CacheStatisticsInterceptor] Logging cache statistics for: " + methodName);
        cacheStatisticsService.logCacheStatistics("Before: " + methodName);

        Object result = context.proceed();

        cacheStatisticsService.logCacheStatistics("After: " + methodName);

        return result;
    }

    private LogCacheStatistics getAnnotation(InvocationContext context) {
        LogCacheStatistics methodAnnotation = context.getMethod().getAnnotation(LogCacheStatistics.class);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }
        return context.getTarget().getClass().getAnnotation(LogCacheStatistics.class);
    }
}

