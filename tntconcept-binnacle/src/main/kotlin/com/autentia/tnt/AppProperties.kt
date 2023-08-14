package com.autentia.tnt

import io.micronaut.context.annotation.ConfigurationProperties
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty

@ConfigurationProperties("app")
internal class AppProperties {

    @NotBlank
    var version: String = ""

    var files = FilesProperties()
    var mail = MailProperties()
    var binnacle = BinnacleProperties()

    @ConfigurationProperties("files")
    internal class FilesProperties {
        @NotBlank
        var attachmentsPath: String = ""

        @NotEmpty
        var supportedMimeTypes: Map<String, String> = emptyMap()
    }

    @ConfigurationProperties("mail")
    internal class MailProperties {
        var enabled: Boolean = false

        @Email
        var from: String = ""
    }

    @ConfigurationProperties("binnacle")
    internal class BinnacleProperties {
        @Email
        var vacationsApprovers: List<String> = emptyList()

        @Email
        var activitiesApprovers: List<String> = emptyList()

        var workSummary = WorkSummaryProperties()
        var missingEvidencesNotification = MissingEvidencesNotificationProperties()

        @ConfigurationProperties("work-summary")
        internal class WorkSummaryProperties {
            var persistenceEnabled: Boolean = false
            var cronExpression: String? = null
            var report = ReportProperties()
            var mail = Mail()

            @ConfigurationProperties("report")
            internal class ReportProperties {
                @NotBlank
                var path: String = ""

                @NotBlank
                var nameSuffix: String = ""

                var showAlertsOnly: Boolean = false
            }

            @ConfigurationProperties("mail")
            internal class Mail {
                var enabled: Boolean = false
                var to: List<String> = emptyList()
                var sendAlertsOnly: Boolean = false
            }

        }

        @ConfigurationProperties("missing-evidences-notification")
        internal class MissingEvidencesNotificationProperties {
            var enabled: Boolean = false
            var weekly = WeeklyProperties()
            var once = OnceProperties()

            @ConfigurationProperties("weekly")
            internal class WeeklyProperties {
                var cronExpression: String? = null
            }

            @ConfigurationProperties("once")
            internal class OnceProperties {
                var cronExpression: String? = null
            }
        }

    }
}
