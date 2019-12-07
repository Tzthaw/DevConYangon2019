package org.devconmyanmar.devconyangon.data.cache

import org.devconmyanmar.devconyangon.DevConYangonDb
import org.devconmyanmar.devconyangon.data.cache.mapper.SessionTableMapper
import org.devconmyanmar.devconyangon.data.datasource.SessionCacheDataSource
import org.devconmyanmar.devconyangon.data.entity.RoomEntity
import org.devconmyanmar.devconyangon.data.entity.SessionEntity
import org.devconmyanmar.devconyangon.data.entity.SpeakerEntity
import org.devconmyanmar.devconyangon.domain.helper.Zones
import org.devconmyanmar.devconyangon.domain.model.RoomId
import org.devconmyanmar.devconyangon.domain.model.SessionId
import org.devconmyanmar.devconyangon.domain.model.SpeakerId
import org.threeten.bp.LocalDate
import org.threeten.bp.Month
import org.threeten.bp.ZonedDateTime
import javax.inject.Inject

/**
 * Created by Vincent on 11/3/19
 */
class SessionCacheDataSourceImpl @Inject constructor(
  private val db: DevConYangonDb,
  private val sessionTableMapper: SessionTableMapper
) : SessionCacheDataSource {


  override fun putSessionEntities(sessionEntities: List<SessionEntity>) {
    db.transaction {
      sessionEntities.forEach { sessionEntity ->
        db.roomTableQueries.insert_or_replace(
          sessionEntity.room.roomId,
          sessionEntity.room.roomName
        )

        val dateTime = sessionEntity.dateTimeInInstant.atZone(Zones.YANGON)

        db.sessionTableQueries.insert_or_replace(
          sessionEntity.sessionId,
          sessionEntity.sessionTitle,
          dateTime.toLocalDate(),
          dateTime.toLocalTime(),
          sessionEntity.room.roomId
        )
        sessionEntity.speakers.forEach { speakerEntity ->
          db.speakerTableQueries.insert_or_replace(speakerEntity.speakerId, speakerEntity.name)
          db.sessionSpeakerTableQueries.insert_or_replace(
            sessionEntity.sessionId,
            speakerEntity.speakerId
          )
        }
      }
    }
  }

  override fun getSessionEntities(date: LocalDate): List<SessionEntity> {
    return db.sessionTableQueries.select_all_at_date(date).executeAsList()
      .map(sessionTableMapper::map)
  }

  override fun getFavoriteSessionEntities(date: LocalDate): List<SessionEntity> {
    return db.sessionTableQueries.select_favorite_at_date(date).executeAsList()
      .map(sessionTableMapper::map)
  }

  override fun getFavoriteStatus(sessionId: SessionId): Boolean {
    val queryResult =
      db.favoriteSessionTableQueries.select_with_session_id(sessionId).executeAsOneOrNull()
    return queryResult != null
  }

  override fun updateFavoriteStatus(sessionId: SessionId, favoriteStatus: Boolean) {

    if (favoriteStatus) {
      db.favoriteSessionTableQueries.insert_or_replace(sessionId)
    } else {
      db.favoriteSessionTableQueries.delete_with_session_id(sessionId)
    }
  }

  override fun getSessionDetail(sessionId: SessionId): SessionEntity {
     return SessionEntity(
       sessionId = SessionId(0),
       sessionTitle = "Building a robust web System",
       dateTimeInInstant = ZonedDateTime.of(
         2019,
         Month.DECEMBER.value,
         21,
         9,
         0,
         0,
         0,
         Zones.YANGON
       ).toInstant(),
       room = RoomEntity(
         roomId = RoomId(0),
         roomName = "Main Hall"
       ),
       speakers = listOf(
         SpeakerEntity(
           speakerId = SpeakerId(0),
           name = "Fake Wharton"
         )
       ),
       isFavorite = false
     )
  }
}

