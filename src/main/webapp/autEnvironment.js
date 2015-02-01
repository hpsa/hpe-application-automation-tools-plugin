/**
 * Created by barush on 09/11/2014.
 */

function checkCorrespondingCheckbox(e) {

    var parentTableNode = getAncestorByTagName(e, 'table');
    var nearestCheckbox = parentTableNode.getElementsByClassName('autEnvParameterCheckbox')[0];

    if (e.value === 'From JSON') {
        nearestCheckbox.style.display = 'inline';
    } else {
        nearestCheckbox.style.display = 'none';
    }
}

function updateValuesOnLoad() {
    var elementsByClassName = document.getElementsByClassName('autEnv');
    var i;
    for (i = 0; elementsByClassName.length > i; i++) {
        checkCorrespondingCheckbox(elementsByClassName[i]);
    }
}

function getAncestorByTagName(el, tn) {
    tn = tn.toLowerCase();
    if (el.parentNode) {
        if (el.parentNode.nodeType == 1
            && el.parentNode.tagName.toLowerCase() == tn
            ) return el.parentNode;
        return getAncestorByTagName(el.parentNode, tn);
    }
    return null
}


setTimeout(updateValuesOnLoad(), 0);

