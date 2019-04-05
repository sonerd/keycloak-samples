<#import "/spring.ftl" as spring>
<html>
<h1>My Favourites</h1>
<ul>
    <#list favourites as favourite>
        <li>${favourite}</li>
    </#list>
</ul>
<br>
<a href="/logout">Logout</a>
</html>