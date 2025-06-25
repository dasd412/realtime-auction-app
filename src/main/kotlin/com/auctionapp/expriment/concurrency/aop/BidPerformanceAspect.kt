package com.auctionapp.expriment.concurrency.aop

import com.auctionapp.com.auctionapp.expriment.concurrency.ConcurrencyControlStrategyRegistry
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Aspect
@Component
class BidPerformanceAspect {
    @Autowired
    private lateinit var strategyRegistry: ConcurrencyControlStrategyRegistry
    private val logger = LoggerFactory.getLogger(BidPerformanceAspect::class.java)
    private val timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

    // 입찰 메서드 실행 시간 측정
    @Around("execution(* com.auctionapp.application.service.AuctionAppService.placeBid(..))")
    fun measureBidPerformance(joinPoint: ProceedingJoinPoint): Any {
        val startTime = System.currentTimeMillis()
        val strategyName = getCurrentStrategyName()
        val args = joinPoint.args
        val auctionId = args[1] as Long // 두 번째 인자는 auctionId

        try {
            val now = LocalDateTime.now().format(timeFormatter)
            logger.info("[$now] 입찰 시작 - 전략: $strategyName, 경매 ID: $auctionId")

            val result = joinPoint.proceed()

            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime
            logger.info("[$now] 입찰 완료 - 전략: $strategyName, 경매 ID: $auctionId, 소요시간: ${duration}ms")

            // CSV 파일 또는 DB에 성능 데이터 저장 (옵션)
            saveBidPerformanceData(strategyName, auctionId, duration, true)

            return result
        } catch (e: Exception) {
            val now = LocalDateTime.now().format(timeFormatter)
            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime
            logger.error("[$now] 입찰 실패 - 전략: $strategyName, 경매 ID: $auctionId, 소요시간: ${duration}ms, 오류: ${e.message}")

            // 실패한 경우도 데이터 저장
            saveBidPerformanceData(strategyName, auctionId, duration, false, e.javaClass.simpleName)

            throw e
        }
    }

    // 현재 사용 중인 전략 이름 가져오기
    private fun getCurrentStrategyName(): String {
        return strategyRegistry.getCurrentStrategyName() // 실제 레지스트리에서 가져옴
    }

    // 성능 데이터 저장 (로그 파일, CSV 또는 DB)
    private fun saveBidPerformanceData(
        strategy: String,
        auctionId: Long,
        durationMs: Long,
        success: Boolean,
        errorType: String? = null,
    ) {
        val timestamp = LocalDateTime.now().format(timeFormatter)
        val record = "$timestamp,$strategy,$auctionId,$durationMs,$success,${errorType ?: "none"}"

        // 간단히 로깅으로만 처리
        logger.info("성능 기록: $record")

        // todo 나중에 실제 데이터 저장 방식 구현
    }
}
