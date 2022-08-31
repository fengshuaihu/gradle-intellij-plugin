package patches.projects

import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.Project
import jetbrains.buildServer.configs.kotlin.projectFeatures.GitHubIssueTracker
import jetbrains.buildServer.configs.kotlin.projectFeatures.githubIssues
import jetbrains.buildServer.configs.kotlin.ui.*

/*
This patch script was generated by TeamCity on settings change in UI.
To apply the patch, change the root project
accordingly, and delete the patch script.
*/
changeProject(DslContext.projectId) {
    check(description == "") {
        "Unexpected description: '$description'"
    }
    description = "Gradle plugin for building plugins for IntelliJ-based IDEs – https://github.com/JetBrains/gradle-intellij-plugin"

    features {
        val feature1 = find<GitHubIssueTracker> {
            githubIssues {
                id = "PROJECT_EXT_621"
                displayName = "JetBrains/gradle-intellij-plugin"
                repositoryURL = "https://github.com/JetBrains/gradle-intellij-plugin"
            }
        }
        feature1.apply {
        }
        add {
            feature {
                type = "Invitation"
                id = "PROJECT_EXT_626"
                param("createdByUserId", "31121")
                param("invitationType", "joinProjectInvitation")
                param("roleId", "PROJECT_ADMIN")
                param("secure:token", "credentialsJSON:a0e64760-bbc5-4682-aca8-e77efbcb5eff")
                param("name", "Join project")
                param("welcomeText", "Jakub Chrzanowski invites you to join the Open-source projects / Gradle IntelliJ Plugin project")
                param("disabled", "false")
                param("multi", "true")
            }
        }
    }
}