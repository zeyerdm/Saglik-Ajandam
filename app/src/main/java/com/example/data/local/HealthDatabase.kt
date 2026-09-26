package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AssistantMessage
import com.example.data.model.HealthNotebook
import com.example.data.model.LabTest
import com.example.data.model.NotebookEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        HealthNotebook::class,
        NotebookEntry::class,
        LabTest::class,
        AssistantMessage::class
    ],
    version = 1,
    exportSchema = false
)
abstract class HealthDatabase : RoomDatabase() {
    abstract fun notebookDao(): HealthNotebookDao
    abstract fun entryDao(): NotebookEntryDao
    abstract fun labTestDao(): LabTestDao
    abstract fun assistantMessageDao(): AssistantMessageDao

    companion object {
        @Volatile
        private var INSTANCE: HealthDatabase? = null

        fun getDatabase(context: Context): HealthDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HealthDatabase::class.java,
                    "saglik_ajandasi.db"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        prepopulateDatabase(database)
                    }
                }
            }
        }

        suspend fun prepopulateDatabase(db: HealthDatabase) {
            val notebookDao = db.notebookDao()
            val entryDao = db.entryDao()
            val labTestDao = db.labTestDao()
            val assistantDao = db.assistantMessageDao()

            val n1 = notebookDao.insertNotebook(
                HealthNotebook(
                    title = "Tansiyon & Nabız",
                    category = "Kronik Takip",
                    iconName = "heart",
                    unit = "mmHg",
                    targetMin = 90.0,
                    targetMax = 120.0,
                    description = "Büyük ve küçük tansiyon ile kalp ritmi takibi",
                    colorHex = "#E11D48"
                )
            )

            val n2 = notebookDao.insertNotebook(
                HealthNotebook(
                    title = "Migren & Baş Ağrısı",
                    category = "Ağrı & Belirti",
                    iconName = "headache",
                    unit = "1-10 Şiddet",
                    description = "Atak sıklığı, tetikleyiciler ve ağrı düzeyi takibi",
                    colorHex = "#7C3AED"
                )
            )

            val n3 = notebookDao.insertNotebook(
                HealthNotebook(
                    title = "Regl & Döngü Takibi",
                    category = "Döngü",
                    iconName = "calendar",
                    unit = "Gün",
                    description = "Döngü başlangıcı, süresi ve PMS belirtileri",
                    colorHex = "#EC4899"
                )
            )

            val n4 = notebookDao.insertNotebook(
                HealthNotebook(
                    title = "Açlık Kan Şekeri",
                    category = "Kronik Takip",
                    iconName = "activity",
                    unit = "mg/dL",
                    targetMin = 70.0,
                    targetMax = 100.0,
                    description = "Sabah aç karnına ölçülen kan şekeri profili",
                    colorHex = "#0284C7"
                )
            )

            val n5 = notebookDao.insertNotebook(
                HealthNotebook(
                    title = "Günlük Su Tüketimi",
                    category = "Genel",
                    iconName = "droplet",
                    unit = "Bardak",
                    targetMin = 8.0,
                    targetMax = 12.0,
                    description = "Gün boyunca içilen toplam su miktarı",
                    colorHex = "#0D9488"
                )
            )

            val now = System.currentTimeMillis()
            val oneDay = 86400000L

            // Sample entries for Tansiyon
            entryDao.insertEntry(
                NotebookEntry(
                    notebookId = n1,
                    numericValue = 118.0,
                    secondaryValue = 78.0,
                    severity = 2,
                    notes = "Sabah dinlenmiş vaziyette, nabız 72",
                    tags = "Sabah, Dinlenme",
                    timestamp = now - (oneDay * 3)
                )
            )
            entryDao.insertEntry(
                NotebookEntry(
                    notebookId = n1,
                    numericValue = 124.0,
                    secondaryValue = 82.0,
                    severity = 3,
                    notes = "İş sonrası hafif yorgunluk",
                    tags = "Akşam, Kahve Sonrası",
                    timestamp = now - (oneDay * 2)
                )
            )
            entryDao.insertEntry(
                NotebookEntry(
                    notebookId = n1,
                    numericValue = 120.0,
                    secondaryValue = 80.0,
                    severity = 2,
                    notes = "Normal gün, hafif yürüyüş yapıldı",
                    tags = "Öğle",
                    timestamp = now - (oneDay)
                )
            )
            entryDao.insertEntry(
                NotebookEntry(
                    notebookId = n1,
                    numericValue = 119.0,
                    secondaryValue = 79.0,
                    severity = 2,
                    notes = "Ölçüm dengeli seyrediyor",
                    tags = "Sabah, Aç Karnına",
                    timestamp = now
                )
            )

            // Sample entries for Migren
            entryDao.insertEntry(
                NotebookEntry(
                    notebookId = n2,
                    numericValue = 7.0,
                    severity = 7,
                    notes = "Şakaklarda zonklama, ışık hassasiyeti belirgin",
                    tags = "Işık Hassasiyeti, Stres, Uykusuzluk",
                    timestamp = now - (oneDay * 5)
                )
            )
            entryDao.insertEntry(
                NotebookEntry(
                    notebookId = n2,
                    numericValue = 3.0,
                    severity = 3,
                    notes = "Hafif gerilim tipi ağrı, nane yağı ve dinlenme ile geçti",
                    tags = "Yorgunluk",
                    timestamp = now - (oneDay * 1)
                )
            )

            // Sample Initial Lab Tests for Comparison and Timeline
            val sampleTest1Json = """
            [
              {"name":"B12 Vitamini","value":188.0,"unit":"pg/mL","referenceRange":"200 - 900","refMin":200.0,"refMax":900.0,"status":"LOW","explanation":"Hafıza, enerji üretimi ve sinir sistemi için temel vitamindir."},
              {"name":"Ferritin","value":16.0,"unit":"ng/mL","referenceRange":"20 - 200","refMin":20.0,"refMax":200.0,"status":"LOW","explanation":"Vücudun demir rezerv deposudur. Düşüklüğü halsizlik ve saç dökülmesine yol açabilir."},
              {"name":"Hemoglobin","value":13.6,"unit":"g/dL","referenceRange":"12.0 - 16.0","refMin":12.0,"refMax":16.0,"status":"NORMAL","explanation":"Kanda oksijen taşıyan kırmızı hücre proteinidir."},
              {"name":"Açlık Kan Şekeri","value":94.0,"unit":"mg/dL","referenceRange":"70 - 100","refMin":70.0,"refMax":100.0,"status":"NORMAL","explanation":"Hücrelere enerji sağlayan glukoz düzeyidir."},
              {"name":"TSH (Tiroid)","value":2.4,"unit":"uIU/mL","referenceRange":"0.4 - 4.2","refMin":0.4,"refMax":4.2,"status":"NORMAL","explanation":"Metabolizma hızını yöneten tiroid uyarıcı hormondur."}
            ]
            """.trimIndent()

            labTestDao.insertLabTest(
                LabTest(
                    title = "Rutin Biyokimya & Vitamin Paneli",
                    testDate = "15 Haziran 2026",
                    laboratory = "Şehir Hastanesi Laboratuvarı",
                    rawText = "B12: 188 pg/mL, Ferritin: 16 ng/mL, Hemoglobin: 13.6 g/dL, Açlık Şekeri: 94 mg/dL, TSH: 2.4",
                    aiSummary = "Tahlilinizde Hemoglobin, Kan Şekeri ve TSH değerleriniz referans aralıklarında gayet sağlıklıdır. Ancak Ferritin (16 ng/mL) ve B12 (188 pg/mL) referans alt sınırının biraz altında seyretmektedir. Bu durum gün içindeki hafif yorgunluk hissinizi açıklayabilir.",
                    doctorQuestions = "• B12 ve Ferritin düşüklüğüm için beslenme düzeni veya oral takviye önerir misiniz?\n• Kontrol tahlilini kaç ay sonra tekrarlamalıyız?",
                    parametersJson = sampleTest1Json,
                    timestamp = now - (oneDay * 75)
                )
            )

            val sampleTest2Json = """
            [
              {"name":"B12 Vitamini","value":340.0,"unit":"pg/mL","referenceRange":"200 - 900","refMin":200.0,"refMax":900.0,"status":"NORMAL","explanation":"Hafıza, enerji üretimi ve sinir sistemi için temel vitamindir."},
              {"name":"Ferritin","value":42.0,"unit":"ng/mL","referenceRange":"20 - 200","refMin":20.0,"refMax":200.0,"status":"NORMAL","explanation":"Vücudun demir rezerv deposudur. 16'dan 42'ye yükselerek sağlıklı seviyeye ulaşmıştır."},
              {"name":"Hemoglobin","value":14.1,"unit":"g/dL","referenceRange":"12.0 - 16.0","refMin":12.0,"refMax":16.0,"status":"NORMAL","explanation":"Kanda oksijen taşıyan kırmızı hücre proteinidir."},
              {"name":"Açlık Kan Şekeri","value":88.0,"unit":"mg/dL","referenceRange":"70 - 100","refMin":70.0,"refMax":100.0,"status":"NORMAL","explanation":"Hücrelere enerji sağlayan glukoz düzeyidir."},
              {"name":"TSH (Tiroid)","value":2.1,"unit":"uIU/mL","referenceRange":"0.4 - 4.2","refMin":0.4,"refMax":4.2,"status":"NORMAL","explanation":"Metabolizma hızını yöneten tiroid uyarıcı hormondur."}
            ]
            """.trimIndent()

            labTestDao.insertLabTest(
                LabTest(
                    title = "3. Ay Kontrol Tahlili",
                    testDate = "20 Eylül 2026",
                    laboratory = "Merkez Sağlık Laboratuvarı",
                    rawText = "B12: 340 pg/mL, Ferritin: 42 ng/mL, Hemoglobin: 14.1 g/dL, Açlık Şekeri: 88 mg/dL, TSH: 2.1",
                    aiSummary = "Harika bir gelişme! Önceki tahlilinizle kıyaslandığında, Ferritin değeriniz 16 ng/mL'den 42 ng/mL'ye (+%162), B12 değeriniz ise 188 pg/mL'den 340 pg/mL'ye yükselerek sağlıklı referans aralığına girmiştir. Diğer tüm parametreleriniz de ideal aralıktadır.",
                    doctorQuestions = "• Mevcut beslenme alışkanlıklarımı sürdürmem yeterli olacak mıdır?\n• Bir sonraki genel kontrol periyodumuz ne zaman olmalı?",
                    parametersJson = sampleTest2Json,
                    timestamp = now - (oneDay * 6)
                )
            )

            // Initial greeting message in assistant
            assistantDao.insertMessage(
                AssistantMessage(
                    isUser = false,
                    message = "Merhaba! Ben Sağlık Ajandası AI Asistanınız. 🌿\n\nTahlillerinizi sade bir dille açıklayabilir, sağlık defterlerinizdeki trendleri özetleyebilir veya yeni bir takip başlatmak istediğinizde ('Adet takibi', 'Migren takibi', 'Tansiyon defteri aç' gibi) size özel şablon oluşturabilirim.\n\nNasıl yardımcı olabilirim?",
                    suggestedNotebookJson = null,
                    timestamp = now - 1000
                )
            )
        }
    }
}
