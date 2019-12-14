package org.devconmyanmar.devconyangon.domain.repository

import org.devconmyanmar.devconyangon.domain.model.Session
import org.devconmyanmar.devconyangon.domain.model.SessionId
import org.threeten.bp.LocalDate

/**
 * Created by Vincent on 11/3/19
 */
interface SessionRepository {

  suspend fun getSessionListing(date: LocalDate): List<Session>

  suspend fun getSession(sessionId: SessionId): Session

  suspend fun getFavoriteSessions(date: LocalDate): List<Session>

  suspend fun toggleFavoriteStatus(sessionId: SessionId)



}
