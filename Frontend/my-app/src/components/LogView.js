import React, {useCallback, useMemo, useRef, useState} from 'react';
import {getAllLogEvents} from '../services/logService';
import {AgGridReact} from 'ag-grid-react';
import 'ag-grid-community/styles/ag-grid.css';
import 'ag-grid-community/styles/ag-theme-material.css';
import {Box, Button, FormControl, InputLabel, MenuItem, Paper, Select, TextField, Typography} from '@mui/material';

const LogView = () => {
    const [gridSize, setGridSize] = useState(1000);
    const [rowData, setRowData] = useState([]);
    const [hasMoreData, setHasMoreData] = useState(false);
    const [isDataFetched, setIsDataFetched] = useState(false);
    const [fromTimestamp, setFromTimestamp] = useState('');
    const [toTimestamp, setToTimestamp] = useState('');
    const [traceId, setTraceId] = useState('');
    const [spanId, setSpanId] = useState('');
    const gridRef = useRef(null);
    const [page, setPage] = useState(0);


    // Styles defined using useMemo for optimization
    const styles = useMemo(() => ({
        gridStyle: {height: '400px', width: '100%', marginTop: '20px'},
        paperStyle: {padding: '20px', margin: '20px 0'},
        inputStyle: {minWidth: 180}
    }), []);

    const fetchData = useCallback(async (loadMore = false) => {
        try {
            console.log('Fetch Data called', {traceId, spanId, fromTimestamp, toTimestamp, page, gridSize});
            const nextPage = loadMore ? page + 1 : page;
            const response = await getAllLogEvents(traceId, spanId, fromTimestamp, toTimestamp, nextPage, gridSize);
            console.log("response", response);
            setRowData(prevData => loadMore ? [...prevData, ...response.data.content] : response.data.content);
            setIsDataFetched(true);
            setHasMoreData(response.data.hasMore);
            if (loadMore) {
                setPage(nextPage);
            }
        } catch (error) {
            console.error('Error fetching logs:', error);
        }
    }, [traceId, spanId, fromTimestamp, toTimestamp, page, gridSize]);


    const resetData = useCallback(() => {
        setRowData([]);
        setIsDataFetched(false);
        setHasMoreData(false);
        setFromTimestamp('');
        setToTimestamp('');
        setTraceId('');
        setSpanId('');
    }, []);

    const setNow = useCallback(() => {
        setToTimestamp(new Date().toISOString().slice(0, 16));
    }, []);

    // Grid ready event handler
    const onGridReady = useCallback(params => {
        params.api.sizeColumnsToFit();
    }, []);

    // Utility functions
    const formatTimestamp = useCallback(date => date.toISOString().slice(0, 16), []);
    const setTimeRange = useCallback(minutes => {
        const now = new Date();
        setToTimestamp(formatTimestamp(now));
        setFromTimestamp(formatTimestamp(new Date(now.getTime() - minutes * 60000)));
    }, [formatTimestamp]);

    // Input change handlers
    const onTraceIdChange = e => setTraceId(e.target.value);
    const onSpanIdChange = e => setSpanId(e.target.value);

    // Grid Size Selection Handler
    const handleGridSizeChange = (event) => {
        setGridSize(event.target.value);
    };

    const columns = useMemo(() => [
        {
            headerName: "Trace ID",
            field: "key.traceId",
            sortable: true,
            filter: true,
        },
        {
            headerName: "Span ID",
            field: "key.spanId",
            sortable: true,
            filter: true,
        },
        {
            headerName: "Timestamp",
            field: "key.timestamp",
            sortable: true,
            filter: 'agDateColumnFilter',
        },
        {
            headerName: "Level",
            field: "level",
            sortable: true,
            filter: true,
        },
        {
            headerName: "Message",
            field: "message",
            sortable: true,
            filter: true,
        },
        {
            headerName: "Resource ID",
            field: "resourceId",
            sortable: true,
            filter: true,
        },
        {
            headerName: "Commit",
            field: "commit",
            sortable: true,
            filter: true,
        },
        {
            headerName: "Metadata",
            field: "metadata",
            cellRenderer: 'agGroupCellRenderer',
        },
    ], []);


    return (
        <Paper style={styles.paperStyle}>
            <Typography variant="h6" style={{marginBottom: '20px'}}>Log Viewer</Typography>
            <Box display="flex" flexWrap="wrap" gap={2} alignItems="center" justifyContent="center">
                <TextField label="Trace ID" variant="outlined" value={traceId}
                           onChange={(e) => setTraceId(e.target.value)} style={{minWidth: 180}}/>
                <TextField label="Span ID" variant="outlined" value={spanId} onChange={(e) => setSpanId(e.target.value)}
                           style={{minWidth: 180}}/>
                <TextField
                    label="Start Time"
                    type="datetime-local"
                    variant="outlined"
                    value={fromTimestamp}
                    onChange={(e) => setFromTimestamp(e.target.value)}
                    style={{minWidth: 250}}
                    InputLabelProps={{shrink: true}} // Ensure label always floats
                />
                <TextField
                    label="End Time"
                    type="datetime-local"
                    variant="outlined"
                    value={toTimestamp}
                    onChange={(e) => setToTimestamp(e.target.value)}
                    style={{minWidth: 250}}
                    InputLabelProps={{shrink: true}} // Ensure label always floats
                />
                <FormControl style={{minWidth: 180}}>
                    <InputLabel>Time Range</InputLabel>
                    <Select value="" onChange={(e) => setTimeRange(e.target.value)} label="Time Range">
                        <MenuItem value={15}>Last 15 Minutes</MenuItem>
                        <MenuItem value={60}>Last 1 Hour</MenuItem>
                        <MenuItem value={60 * 6}>Last 6 Hour</MenuItem>
                        <MenuItem value={60 * 12}>Last 12 Hour</MenuItem>
                        <MenuItem value={60 * 24}>Last 24 Hours</MenuItem>
                        <MenuItem value={60 * 24 * 7}>Last 7 Days</MenuItem>
                        <MenuItem value={60 * 24 * 14}>Last 14 Days</MenuItem>
                    </Select>
                </FormControl>
                <FormControl style={{minWidth: 180}}>
                    <InputLabel>Grid Size</InputLabel>
                    <Select value={gridSize} onChange={handleGridSizeChange} label="Grid Size">
                        <MenuItem value={1500}>1500 Rows</MenuItem>
                        <MenuItem value={2000}>2000 Rows</MenuItem>
                        <MenuItem value={5000}>5000 Rows</MenuItem>
                        <MenuItem value={10000}>10000 Rows</MenuItem>
                    </Select>
                </FormControl>
                <Button onClick={setNow} variant="contained">Now</Button>
                <Button onClick={() => fetchData(false)} variant="contained">Fetch Logs</Button>
                <Button onClick={resetData} variant="contained">Reset</Button>
            </Box>
            <div style={styles.gridStyle}>
                <AgGridReact
                    onGridReady={onGridReady}
                    ref={gridRef}
                    columnDefs={columns}
                    rowData={rowData}
                    animateRows={true}
                    paginationPageSize={gridSize}  // Use gridSize here
                    sortable={true}
                    filter={true}
                />
            </div>
            {isDataFetched && hasMoreData && (
                <Button onClick={() => fetchData(true)} variant="contained" style={{marginTop: '10px'}}>
                    Load More Logs
                </Button>
            )}
        </Paper>
    );
};

export default LogView;
