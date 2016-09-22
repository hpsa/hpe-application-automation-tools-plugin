// var React = require('react');
// var ReactDOM = require('react-dom');

// var Chart = require('chart.js');

instance.getTotalHitsGraphData(function(t){
    var r = t.responseObject();
    var ctx = document.getElementById('TotalHits').getContext("2d");
    new Chart(ctx).Line(r);
});


instance.getAvgHitsPerSecGraphData(function(t){
    var data = t.responseObject();

    var option = {};
    var ctx = document.getElementById('AvgHitsPerSecGraphData').getContext("2d");
    new Chart(ctx).Line(data);
});

instance.getTotalThroughputGraphData(function(t){
    var data = t.responseObject();
    var ctx = document.getElementById('TotalThroughput').getContext("2d");
    new Chart(ctx).Line(data);

});

instance.getAvgThroughtputResultsGraphData(function(t){
    var data = t.responseObject();
    var ctx = document.getElementById('AvgThroughput').getContext("2d");
    new Chart(ctx).Line(data);

});


instance.getAvgTransactionResultsGraphData(function(t){
    var data = t.responseObject();
    var ctx = document.getElementById('AvgTRT').getContext("2d");
    var a = new Chart(ctx).Line(data);

    document.getElementById('AvgTRT_Legend').innerHTML = a.generateLegend();

});

instance.getErrorPerSecResultsGraphData(function(t){
    var data = t.responseObject();
        var ctx = document.getElementById('ErrorPerSec').getContext("2d");
        new Chart(ctx).Line(data);
});

instance.getPercentelieTransactionResultsGraphData(function(t){
    var data = t.responseObject();
        var ctx = document.getElementById('PercentileTransactionWholeRun').getContext("2d");
        var a = new Chart(ctx).Line(data);
        document.getElementById('').innerHTML = a.generateLegend();
});

instance.getPercentelieTransactionResultsGraphData(function(t) {
    var data = t.responseObject();
    // document.getElementById('msr').innerHTML = ((data.labels));
    // document.getElementById('msv').innerHTML = JSON.stringify((data));'\
    console.log(Object.keys(data)[1]);
    console.log(Object.values(data[1]).length);
});


// var adf = JSON.parse(totalHitsGraph2);
// document.getElementById('mse').innerHTML = JSON.stringify(resultc);
// document.getElementById('msr').innerHTML = JSON.stringify(adf);
//
//
// var Hello = React.createClass({
//     displayName: 'Hello',
//     render: function(){
//         return React.createElement('div', null, 'Hello ', this.props.name);
//     }
// });
//
// ReactDOM.render(
//     React.createElement(Hello, { name: 'World' }),
//     document.getElementById('container')
// );
//
//
// var ExampleApplication = React.createClass({
//     render: function () {
//         var elapsed = Math.round(this.props.elapsed / 100);
//         var seconds = elapsed / 10 + (elapsed % 10 ? '' : '.0' );
//         var message = "React has been successfully running for " + seconds + "seconds.";
//
//         return React.DOM.p(null, message);
//     }
// });
//
// // Call React.createFactory instead of directly call ExampleApplication({...}) in React.render
// var ExampleApplicationFactory = React.createFactory(ExampleApplication);
//
// var start = new Date().getTime();
// setInterval(function () {
//     ReactDOM.render(
//         ExampleApplicationFactory({ elapsed: new Date().getTime() - start }),
//         document.getElementById('container')
//     );
// }, 50);



//
//
// var cxz = {"2013-01-21":1,"2013-01-22":7};
// var resultc = [];
//
//
//
// for(var i in cxz)
//     resultc.push([i,cxz[i]]);
//
//
//
//
//
// instance.getTotalHitsGraphData(function(t) {
//     document.getElementById('msk').innerHTML = JSON.stringify(JSON.parse(t.responseObject()));
// });
//
//
// document.getElementById('msr').innerHTML = JSON.stringify(totalHitsGraphData);

