package com.auctionapp.infrastructure.scheduler.job

import com.auctionapp.application.exception.NotFoundAuctionException
import com.auctionapp.domain.service.AuctionService
import com.auctionapp.infrastructure.persistence.AuctionRepository
import org.quartz.JobExecutionContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.quartz.QuartzJobBean
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class AuctionEndJob : QuartzJobBean() {
    @Autowired
    private lateinit var auctionService: AuctionService

    @Autowired
    private lateinit var auctionRepository: AuctionRepository

    @Transactional
    override fun executeInternal(context: JobExecutionContext) {
        val jobDataMap = context.mergedJobDataMap
        val auctionId = jobDataMap.getLong("auctionId")

        val auction = auctionRepository.findByIdOrNull(auctionId) ?: throw NotFoundAuctionException()
        auctionService.endAuction(auction)
    }
}
