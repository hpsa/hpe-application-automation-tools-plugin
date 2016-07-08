var builder = require('@jenkins-cd/js-builder');

//
// Bundle the modules.
// See https://github.com/jenkinsci/js-builder
//
// For example builder.bundle('src/main/js/jslib-samples.js');


// set the language to v 6 for react

builder.lang([6]);
builder.bundle('src/main/js/jslib.js');
