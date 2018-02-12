import React, {Component} from 'react';
import {
    View,
    StyleSheet,
    ScrollView,
    RefreshControl,
    Text,
    Image,
    Keyboard,
    TouchableOpacity,
    Animated
} from 'react-native';
import ListItemComponent from '../components/ListItemComponent';
import ListItemModel from '../models/ListItemModel';
import ExpanderComponent from '../components/ExpanderComponent';
import PropTypes from 'prop-types';
import { getWidth, getHeight } from '../utils/adaptive';

/**
* Custom List component
*/
export default class ListComponent extends Component {
    constructor(props) {
        super(props); 

        this.state = {
            refreshing: false,
            selectedItemId: null        
        };
    }

    componentWillMount () {
        this.keyboardDidShowListener = Keyboard.addListener('keyboardDidShow', () => { this.setState({ selectedItemId: null }); });
    }
    
    componentWillUnmount () {
        this.keyboardDidShowListener.remove();
    }

    /**
    * Fires on long press
    * @param {object} item type of ListItemModel
    */
    onItemLongPress(item) {
        this.props.enableSelectionMode();
        this.selectItem(item);
    }   

    /**
    * Fires on swipe from top to bottom to refresh the data
    */
    onRefresh() {
        this.setState({refreshing: true});
        
        //TODO: call getData function

        this.setState({refreshing: false});
    }

    /**
    * Fires on long press
    * @param {object} item type of ListItemModel
    */
    selectItem(selectedItem) {
        /* if(!this.props.isSelectionMode) return; */
        
        if(selectedItem.isSelected)
            this.props.deselectItem(selectedItem);
        else
            this.props.selectItem(selectedItem);
    }

    sortByDate(items, sortingObject) {
        let monthNames = [
            "January", "February", "March",
            "April", "May", "June", "July",
            "August", "September", "October",
            "November", "December"
        ];

          items.forEach((item) => {
            var date = new Date(item.getDate());
            
            var day = date.getDate();
            var monthIndex = date.getMonth();
            var year = date.getFullYear();

            var prop = day + ' ' + monthNames[monthIndex] + ' ' + year;            
            
            if(!sortingObject[prop]) {
                sortingObject[prop] = [];
            }

            sortingObject[prop].push(item);
        });
    }

    sortByName(items, sortingObject) {        
        let rows = [];
        rows = [...new Set(items.map((item) => {
            let firstChar = item.getName().charAt(0);
            if(rows.exists(firstChar.toUpperCase()))
                return ;
        }))];

        console.log(rows);        

        items.forEach((item) => {
            var firstLetter = item.getName()[0];

            var prop = item.getName().charAt(0);            
            
            if(!sortingObject[prop]) {
                sortingObject[prop] = [];
            }

            sortingObject[prop].push(item);
        });
    }

    sort(items) {
        let sortingObject = {};
        let sortingCallback;

        switch('date') {
            case 'date': sortingCallback = this.sortByDate;
                break;
            case 'name': sortingCallback = this.sortByName;
                break;
        }
        
        sortingCallback(items, sortingObject);
        
        return sortingObject;
    }

    getItemsList() {
        let sorting = this.sort(this.props.data);

        return Object.getOwnPropertyNames(sorting).reverse().map((propName, index) => {
            return (
                <View key = { propName }>
                    {
                        (() => {
                            let prop = sorting[propName];
                            if(Array.isArray(prop) && prop.length) {
                                var listItems = prop.map((item, indexInner) => {
                                    return(
                                        <ListItemComponent
                                            key = { item.entity.id }
                                            item = { item } 
                                            isFileLoading = { false }
                                            selectItemId = { (itemId) => { this.setState({ selectedItemId: itemId }) }}
                                            navigateToFilesScreen = { this.props.navigateToFilesScreen ? this.props.navigateToFilesScreen : () => {} }
                                            isItemActionsSelected = { this.state.selectedItemId === item.getId() }
                                            onLongPress = { () => { this.onItemLongPress(item); } }
                                            isSelectionModeEnabled = { this.props.isSelectionMode }
                                            isSelected = { item.isSelected }
                                            isSingleItemSelected = { this.props.isSingleItemSelected }
                                            disableSelectionMode = { this.props.disableSelectionMode }
                                            progress = { 0 }
                                            listItemIcon = { this.props.listItemIcon }
                                            onSelectionPress = { () => { this.selectItem(item); } }
                                            onPress = { this.props.onPress }
                                            onSingleItemSelected = { this.props.onSingleItemSelected } />
                                    );
                                });
                                return(
                                    <ExpanderComponent
                                        propName = { propName } 
                                        listItems = { listItems } />
                                );
                            }
                        })()
                    }
                </View>
            );
        });
    }

    render() {        
        return (
            <View>
                <Animated.ScrollView style = { styles.listContainer }
                    scrollEventThrottle = { 16 }
                    style = { styles.listContainer }
                    onScroll = {
                        Animated.event([{
                            nativeEvent: { 
                                    contentOffset: { 
                                        y: this.props.animatedScrollValue 
                                    } 
                                }
                            }
                        ], { useNativeDriver: true }) }
                    refreshControl={
                        <RefreshControl
                            enabled = { !this.props.isSelectionMode }
                            refreshing = { this.state.refreshing }
                            onRefresh = { this.onRefresh.bind(this) } /> }>

                            <View style = { styles.contentWrapper }>
                                {
                                    this.getItemsList()
                                }
                            </View>
                </Animated.ScrollView>
            </View>            
        );
    }
}

//TODO: check if all props are valid
ListComponent.propTypes = {
    data: PropTypes.array,
    /* onSingleItemSelected: PropTypes.function, */
    /* selectItem: PropTypes.function,
    deselectItem: PropTypes.function, */
    listItemIcon: PropTypes.number, //wtf?
    mainTitlePath: PropTypes.string,
    sortOptions: PropTypes.string,
    idPath: PropTypes.string
};

const styles = StyleSheet.create({
    listContainer: {
        backgroundColor: 'white',
    },
    contentWrapper: {
        paddingVertical: getHeight(70)
    }
});
