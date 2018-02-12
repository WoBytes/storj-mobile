import React, { Component } from 'react';
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import { bucketsListContainerAction } from '../reducers/mainContainer/mainReducerActions';
import { navigateToFilesScreen, navigateBack } from '../reducers/navigation/navigationActions';
import BucketsListComponent from '../components/BucketsListComponent';

class BucketsListContainer extends Component {
    constructor(props) {
        super(props);
    }

    static navigationOptions = {
        header: null
    };

    onPress(params) {
        this.props.navigateToFilesScreen(params.bucketId);    
    }

    render() {
        return(
            <BucketsListComponent
                onPress = { (params) => { this.onPress(params); } }
                animatedScrollValue = { this.props.screenProps.animatedScrollValue }
                onSingleItemSelected = { this.props.onSingleItemSelected }
                enableSelectionMode = { this.props.enableSelectionMode }
                disableSelectionMode = { this.props.disableSelectionMode }
                isSelectionMode = { this.props.isSelectionMode }
                isSingleItemSelected = { this.props.isSingleItemSelected }
                deselectBucket = { this.props.deselectBucket }
                selectBucket = { this.props.selectBucket }
                buckets = { this.props.buckets } />
        );
    }
}

function mapStateToProps(state) {
    return {
        isSelectionMode: state.mainReducer.isSelectionMode,
        isSingleItemSelected: state.mainReducer.isSingleItemSelected,
        buckets: state.mainReducer.buckets
    };
}

function mapDispatchToProps(dispatch) {
    return bindActionCreators({ ...bucketsListContainerAction, navigateToFilesScreen }, dispatch);
}

export default connect(mapStateToProps, mapDispatchToProps)(BucketsListContainer);