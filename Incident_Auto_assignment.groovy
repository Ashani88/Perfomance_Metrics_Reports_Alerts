// Import necessary classes
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.security.groups.GroupManager
import com.atlassian.jira.issue.search.SearchProvider
import com.atlassian.query.Query
import com.atlassian.jira.issue.search.SearchResult
import com.atlassian.jira.event.type.EventDispatchOption

// Define the Jira group to get users from
def groupName = "ABC_Experts"  

// Get the Group Manager and User Manager
def groupManager = ComponentAccessor.getGroupManager()
def userManager = ComponentAccessor.getUserManager()

//  list of users in the specified group
def group = groupManager.getGroup(groupName)
if (group == null) {
    log.error("Group '${groupName}' not found!")
    return
}

def usersInGroup = groupManager.getUsersInGroup(group)
if (usersInGroup.isEmpty()) {
    log.error("No users found in the group '${groupName}'.")
    return
}

// round-robin assignment 
def counter = 0
def usersList = usersInGroup.toList()

// issue manager and search provider
def issueManager = ComponentAccessor.getIssueManager()
def searchProvider = ComponentAccessor.getComponent(SearchProvider)
def queryParser = ComponentAccessor.getComponent(com.atlassian.jira.jql.parser.JqlQueryParser)
def query = queryParser.parseQuery("status = 'Open' AND assignee is EMPTY") // Modify this JQL query based on your requirements

// search for unassigned issues
SearchResult searchResult = searchProvider.search(query, ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser(), com.atlassian.jira.permission.PermissionSchemeManager.ALL_PERMISSIONS)

// Loop the unassigned issues and assign them to users in the group
searchResult.getIssues().each { issue ->
    if (issue.assignee == null) {
        // Select the user to assign (round-robin or sequential assignment)
        ApplicationUser userToAssign = usersList[counter % usersList.size()]
        
        // Assign the issue to the selected user
        issue.setAssignee(userToAssign)
        
        // Update the issue in Jira
        issueManager.updateIssue(userToAssign, issue, EventDispatchOption.ISSUE_UPDATED, false)
        
        // Log the assignment
        log.info("Assigned issue ${issue.key} to ${userToAssign.displayName}")
        
        // Increment the counter for the next user in the round-robin sequence
        counter++
    }
}
