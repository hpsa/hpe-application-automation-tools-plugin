import * as React from "react";
/**
 * Created by kazaky on 22/09/2016.
 */

const styles = {
  active: {
      display: 'inherit'
  },
  inactive: {
      display: 'none'
  }
};

class Accordion extends React.component
{
    constructor()
    {
        super();
        this.state =
        {
            active: false
        };
        this.toggle = this.toggle.bind(this);
    }

    toggle(){
        this.setState({active: !this.state.active});
    }

    render()
    {
        const stateStyle = this.state.active ? styles.active : style.inactive;
        return (
            <section>
                <a onClick={this.toggle}> {this.props.summary}</a>
                <p style={stateStyle}>{this.props.details}</p>
            </section>
        );
    }
}

export default Accordion;