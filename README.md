# Perfomance_Metrics_Reports_Alerts

Incident_Auto_assignment.groovy script is having below features: 

a.	Round-robin assignment is done by assigning users in sequence.

b.	Tickets are assigned to jira users in ‘’ ABC_Experts’ group.

c.	The script uses Jira's SearchProvider to fetch issues based on a JQL query.

d.	It checks each issue's assignee and assigns the user if the assignee is empty.
