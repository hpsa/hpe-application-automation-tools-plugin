
class Chart extends React.Component
{
    componentDidMount() {
        this.updateChart(this.props.chartData);
    };
    updateChart() {
        let chartName = this.props.chartName;
        let options = this.optionsSetter();
        return new Chartist.Line('#' + chartName, this.props.chartData, options);
    };

    optionsSetter() {
        let options = new Object();
        options.fullWidth = false;
        options.chartPadding = {right: 40};
        options.plugins = [];
        options.plugins.push(Chartist.plugins.tooltip());
        options.plugins.push(Chartist.plugins.ctAxisTitle({
            axisX: {
                axisTitle: this.props.chartData.x_axis_title,
                axisClass: 'ct-axis-title',
                offset: {
                    x: 0,
                    y: 40
                },
                textAnchor: 'middle'
            },
            axisY: {
                axisTitle: this.props.chartData.y_axis_title,
                axisClass: 'ct-axis-title',
                offset: {
                    x: 0,
                    y: 30
                },
                textAnchor: 'middle',
                flipTitle: false
            }
        }));
        if ((this.props.multiSeriesChart == true)) {
            options.plugins.push(Chartist.plugins.legend({
                position: 'bottom'
            }));
        }
        options.axisY = {
            labelInterpolationFnc: function(value)
            {
                return Chartist.roundWithPrecision(value, 6);
            }
        }
        return options;
    }


    render() {
        return (
               <div id={this.props.chartName} className=' ct-chart ct-minor-seventh'>
                </div>
       );
    }

};




class RenderCharts extends React.Component
{

    render() {
        let graphsData = this.props.graphsData;
        let charts = Object.keys(graphsData).map((chartName, index)=>
        {
            let chartData = graphsData[chartName];
            let multiSeriesChart = false;
            if(chartData.series.length > 1)
            {
                multiSeriesChart = true;
            }
            return <div key = {chartName}>
                <div>
                    <h1>{chartData.title}</h1>
                    <span class="ct-chart-description">{chartData.description}</span>
                </div>
                    <Chart key = {chartName} chartName = {chartName} chartData = {chartData}
                           multiSeriesChart = {multiSeriesChart} {...this.props}/>
                <hr className="ct-chart-seprator"/>
            </div>;
        });
        let returnValue = null;
        if(charts.length > 0)
        {
            returnValue = <div>{charts}</div>;
        }
        return returnValue;



    }
};

class Charts extends React.Component
{
    render()
    {
        return (
            <div id="charts"> <RenderCharts {...this.props}/> </div>)
    }
}

class ChartDashboard extends React.Component{
    static propTypes: {
        globalOptions: React.PropTypes.array,
        graphsData: React.PropTypes.object.isRequired,
        dataProcessFunc: React.PropTypes.func,
        multiSeries: React.PropTypes.boolean,
    };

    static defaultProps : {
        globalOptions: [],
        graphsData: {},
        multiSeries: false,
    };

    render() {
        return (<Charts {...this.props}/>);
    }
};

/**
 * Checks if the given graphs is of SLA rule type that has multiple transaction and therefore
 * requires legend and multiple series support
 * @param graphKey - the graph name (as defined in the data object given.
 * @returns boolean iff it's of multiple transaction type.
 */
function isMultipleTransactionGraph(graphKey) {
    if(graphKey == 'averageTransactionResponseTime' || graphKey == 'percentileTransaction')
    {
        return true;
    }

    return false;
};

/**
 * Updates the graph view per scenario key
 * @param scenarioKey - the selected scenario
 */
function updateGraphs(scenarioKey)
{
    instance.getGraphData(function(t)
    {
        let graphsData = (t.responseObject())[scenarioKey];
        console.log(JSON.stringify(graphsData));
        ReactDOM.render(<ChartDashboard graphsData = {graphsData} dataProcessFunc = {isMultipleTransactionGraph}/>,
            document.querySelector('.graphCon'));
    });
};

var Dropdown = React.createClass({

    propTypes: {
        id: React.PropTypes.string.isRequired,
        options: React.PropTypes.array.isRequired,
        value: React.PropTypes.oneOfType(
            [
                React.PropTypes.object,
                React.PropTypes.string
            ]
        ),
        valueField: React.PropTypes.string,
        labelField: React.PropTypes.string,
        onChange: React.PropTypes.func
    },

    getDefaultProps: function() {
        return {
            value: null,
            valueField: 'value',
            labelField: 'label',
            onChange: null
        };
    },

    getInitialState: function() {
        var selected = this.getSelectedFromProps(this.props);
        // console.log(JSON.stringify(selected));
        updateGraphs(selected);
        return {
            selected: selected
        }
    },

    componentWillReceiveProps: function(nextProps) {
        var selected = this.getSelectedFromProps(nextProps);
        this.setState({
            selected: selected
        });
    },

    getSelectedFromProps(props) {
        var selected;
        if (props.value === null && props.options.length !== 0) {
            selected = props.options[0][props.valueField];
        } else {
            selected = props.value;
        }
        return selected;
    },

    render: function() {
        var self = this;
        var options = self.props.options.map(function(option) {
            return (
                <option key={option[self.props.valueField]} value={option[self.props.valueField]}>
                    {option[self.props.labelField]}
                </option>
            )
        });
        return (
            <select id={this.props.id}
                    className='form-control'
                    value={this.state.selected}
                    onChange={this.handleChange}>
                {options}
            </select>
        )
    },

    handleChange: function(e) {
        if (this.props.onChange) {
            var change = {
                oldValue: this.state.selected,
                newValue: e.target.value
            };
            this.props.onChange(change);
        }
        this.setState({selected: e.target.value});
    }

});

var dropDownOnChange = function(change) {
    updateGraphs(change.newValue);

};

instance.getScenarioList(function(t)
{
    ReactDOM.render(<Dropdown id='myDropdown' options={t.responseObject()} labelField='ScenarioName' valueField='ScenarioName' onChange={dropDownOnChange}/>,
        document.getElementById('scenarioDropDown'));
});
