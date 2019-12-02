(function(document, $, ns) {
"use strict";

$(document).on("click", ".cq-dialog-submit", function(e) {
        var resType = document.getElementsByName('./sling:resourceType');
        if (resType.length > 0 && resType[0].value === 'sc-aem-tags-report/components/content/tagsUtil') {
            e.stopPropagation();
            e.preventDefault();
            var $form = $(this).closest("form.foundation-form"),
                tagsPath = $form.find("[name='./tagsPath']").val(),
                damPath = $form.find("[name='./damLoc']").val();
            var isTagPath = false;
            var valPaths = ["/etc/tags", "/content/cq:tags/"];
            if (tagsPath) {
            	if(!tagsPath.startsWith("/etc/tags") && !tagsPath.startsWith("/content/cq:tags/")) isTagPath = true;
            }

            var isDAMPath = false;
            if (damPath) {
                if (!damPath.startsWith("/content/dam/")) isDAMPath = true;
            }
            
            if (isTagPath || isDAMPath) {
            ns.ui.helpers.prompt({
                title: Granite.I18n.get("Invalid Input"),
                message: "Please select the valid path(s)",
                actions: [{
                    id: "CANCEL",
                    text: "CANCEL",
                    className: "coral-Button"
                }],
                callback: function(actionId) {
                    if (actionId === "CANCEL") {}
                }
            });

        }
    else {
        $form.submit();
    }
}


});

})(document, Granite.$, Granite.author);