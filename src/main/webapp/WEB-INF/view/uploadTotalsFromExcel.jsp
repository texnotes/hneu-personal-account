<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ include file="jspf/header.jspf" %>
<%@ include file="jspf/management-header.jspf" %>

<div class="bs-callout bs-callout-info">
    <h4><spring:message code="form.label.management.add.totals"/></h4>
    <p><spring:message code="form.label.management.add.totals.message"/></p>
    <form method="post" action="/management/uploadTotalsFromExcel" modelAttribute="uploadForm"
          enctype="multipart/form-data">
        <div class="input-group">
        <span class="input-group-btn">
            <span class="btn btn-primary btn-file">
                <spring:message code="form.label.management.browse"/>&hellip;
                <input name="files" type="file" multiple
                       placeholder="<spring:message code="form.label.management.profiles"/>"/>
            </span>
        </span>
            <input type="text" class="form-control" readonly>
        <span class="input-group-btn">
            <input type="submit" value="<spring:message code="form.label.management.download"/>"
                   class="btn btn-default"/>
        </span>
        </div>
    </form>
</div>
<%@ include file="jspf/management-footer.jspf" %>
<%@ include file="jspf/footer.jspf" %>