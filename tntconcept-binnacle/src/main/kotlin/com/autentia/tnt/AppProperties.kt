package com.autentia.tnt

import io.micronaut.context.annotation.ConfigurationProperties
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank

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
        var activityImages: String = ""
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
        var vacationsApprover: String = ""

        var notWorkableProjects: List<Int> = emptyList()
        var workSummary = WorkSummaryProperties()

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
                var to: String = ""
                var sendAlertsOnly: Boolean = false
            }

        }
    }
}
