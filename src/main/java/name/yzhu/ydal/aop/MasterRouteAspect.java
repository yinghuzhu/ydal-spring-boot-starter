package name.yzhu.ydal.aop;

import org.apache.shardingsphere.api.hint.HintManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * @author yhzhu
 */
@Aspect
public class MasterRouteAspect {
    @Pointcut("@annotation(name.yzhu.ydal.annotation.MasterRoute)")
    public void annotationPoinCut(){}

    @Around("annotationPoinCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        try(HintManager hintManager = HintManager.getInstance()){
            hintManager.setMasterRouteOnly();
            return joinPoint.proceed();
        }
    }
}
