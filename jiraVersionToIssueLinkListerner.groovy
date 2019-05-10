import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.IssueManager
import com.atlassian.jira.issue.MutableIssue
import com.atlassian.jira.project.version.Version
import com.atlassian.jira.project.version.VersionManager
import com.atlassian.jira.event.issue.AbstractIssueEventListener
import com.atlassian.jira.event.issue.IssueEvent
import com.atlassian.jira.issue.link.IssueLink;
import com.atlassian.jira.issue.link.IssueLinkManager
import com.atlassian.jira.issue.link.IssueLinkTypeManager 
import org.apache.log4j.Level

log.setLevel(Level.DEBUG)

def issueLinkManager = ComponentAccessor.getComponent(IssueLinkManager) 
def versionManager = ComponentAccessor.getVersionManager()
def currentUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()
def issueLinkTypeManager = ComponentAccessor.getComponent(IssueLinkTypeManager)
def relatesLinkTypeName = "InFixVersion" 
def relateLinkType = issueLinkTypeManager.getIssueLinkTypesByName(relatesLinkTypeName) 
def linkedIssuesNotInVersion = []

Issue updatedIssue = event.getIssue()

List<IssueLink> allOutIssueLink = ComponentAccessor.getIssueLinkManager().getOutwardLinks(updatedIssue.getId())

allOutIssueLink.each{
    if (it instanceof IssueLink) {
        if (it.getIssueLinkType().getId().compareTo((Long) relateLinkType[0].id) ){
                    linkedIssuesNotInVersion.add(it)
        }
    }
}

log.debug("linkedIssuesKeys" + linkedIssuesNotInVersion)

ComponentAccessor.issueLinkManager.removeIssueLinks(updatedIssue, currentUser)

Collection<Version> fixVersions = new ArrayList<Version>()
fixVersions = updatedIssue.getFixVersions()
fixVersions.each {   
    if (it instanceof Version) {
        Collection<Issue> issueInVersion = versionManager.getIssuesWithFixVersion(it)
        issueInVersion.each { 
			if (it instanceof Issue) {                 
                issueLinkManager.createIssueLink((Long) updatedIssue.getId(), (Long) it.getId(), (Long) relateLinkType[0].id, (Long) 1, currentUser) 
                
            }
   		 }	
	}
}

linkedIssuesNotInVersion.each{
    if (it instanceof IssueLink) {                 
        issueLinkManager.createIssueLink((Long) updatedIssue.getId(), (Long) it.getDestinationId(), (Long) it.getLinkTypeId(), (Long) 1, currentUser)           
    }
}
