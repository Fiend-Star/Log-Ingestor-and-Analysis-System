import React, {useEffect, useState, useRef } from 'react';
import {getAllLogEvents} from '../services/logService';
import {AgGridReact} from 'ag-grid-react';
import 'ag-grid-community/styles/ag-grid.css';
import 'ag-grid-community/styles/ag-theme-alpine.css'; // Replace 'ag-theme-alpine' with your preferred theme


const LogView = () => {
    const [rowData, setRowData] = useState([]);
    const [currentPage, setCurrentPage] = useState(1);
    const [pageSize, setPageSize] = useState(10); // Example page size
    const gridRef = useRef(null); // Reference to the AG Grid


    useEffect(() => {
        // Adjust this function to accept pagination and filter parameters
        getAllLogEvents(currentPage - 1, pageSize)
            .then(response => {
                setRowData(response.data);
            })
            .catch(error => {
                console.error('Error fetching logs:', error);
            });
    }, [currentPage, pageSize]);

    const onFilterTextChange = (event) => {
        gridRef.current.api.setQuickFilter(event.target.value);
    };

    const columns = [
        { headerName: "Trace ID", field: "key.traceId", sortable: true, filter: true },
        { headerName: "Span ID", field: "key.spanId", sortable: true, filter: true },
        { headerName: "Timestamp", field: "key.timestamp", sortable: true, filter: 'agDateColumnFilter' },
        { headerName: "Level", field: "level", sortable: true, filter: true },
        { headerName: "Message", field: "message", sortable: true, filter: true },
        { headerName: "Resource ID", field: "resourceId", sortable: true, filter: true },
        { headerName: "Commit", field: "commit", sortable: true, filter: true },
        { headerName: "Metadata", field: "metadata", cellRenderer: 'agGroupCellRenderer' },
    ];

    return (
        <div className="ag-theme-alpine" style={{height: 600, width: '100%'}}>
            <AgGridReact
                ref={gridRef} // Attach the ref to AgGridReact
                columnDefs={columns}
                rowData={rowData}
                animateRows={true}
                pagination={true}
                paginationPageSize={pageSize}
                onGridReady={params => {
                    params.api.sizeColumnsToFit();
                }}
                onPaginationChanged={params => {
                    setCurrentPage(params.api.paginationGetCurrentPage() + 1);
                    setPageSize(params.api.paginationGetPageSize());
                }}
                // Enable sorting and filtering
                sortable={true}
                filter={true}
            />
        </div>
    );
};

export default LogView;
