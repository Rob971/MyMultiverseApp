package com.example.kmp.domain.service

import com.example.kmp.domain.model.SmartGoalProposal

interface AiService {
    suspend fun refineDream(seed: String): Result<SmartGoalProposal>
}
