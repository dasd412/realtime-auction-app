package com.auctionapp.infrastructure.scheduler

import com.auctionapp.domain.entity.Auction
import com.auctionapp.infrastructure.scheduler.job.AuctionEndJob
import com.auctionapp.infrastructure.scheduler.job.AuctionStartJob
import org.quartz.JobBuilder
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.TriggerBuilder
import org.springframework.stereotype.Service
import java.util.*

@Service
class AuctionSchedulerService(
    private val scheduler: Scheduler,
) {
    // 경매 등록 시 시작 및 종료 작업 스케줄링
    fun scheduleAuctionJobs(auction: Auction) {
        if (auction.id == null) {
            throw IllegalArgumentException("경매 id는 null이 될 수 없습니다")
        }

        scheduleAuctionStartJob(auction)

        scheduleAuctionEndJob(auction)
    }

    private fun scheduleAuctionStartJob(auction: Auction) {
        val jobDetail =
            JobBuilder.newJob(AuctionStartJob::class.java)
                .withIdentity("start-auction-${auction.id}", "auction-jobs")
                .usingJobData("auctionId", auction.id!!)
                .storeDurably()
                .build()

        val trigger =
            TriggerBuilder.newTrigger()
                .withIdentity("start-auction-trigger-${auction.id}", "auction-triggers")
                .startAt(Date.from(auction.startTime.atZone(java.time.ZoneId.systemDefault()).toInstant()))
                .build()

        scheduler.scheduleJob(jobDetail, trigger)
    }

    private fun scheduleAuctionEndJob(auction: Auction) {
        val jobDetail =
            JobBuilder.newJob(AuctionEndJob::class.java)
                .withIdentity("end-auction-${auction.id}", "auction-jobs")
                .usingJobData("auctionId", auction.id!!)
                .storeDurably()
                .build()

        val trigger =
            TriggerBuilder.newTrigger()
                .withIdentity("end-auction-trigger-${auction.id}", "auction-triggers")
                .startAt(Date.from(auction.endTime.atZone(java.time.ZoneId.systemDefault()).toInstant()))
                .build()

        scheduler.scheduleJob(jobDetail, trigger)
    }

    // 경매 취소 시 스케줄된 작업 삭제
    fun unScheduleAuctionJobs(auctionId: Long) {
        scheduler.deleteJob(JobKey.jobKey("start-auction-$auctionId", "auction-jobs"))

        scheduler.deleteJob(JobKey.jobKey("end-auction-$auctionId", "auction-jobs"))
    }
}
