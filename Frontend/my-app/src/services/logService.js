import axios from 'axios';

const apiClient = axios.create({
    baseURL: 'http://localhost:3000/',
    timeout: 4000000
});


export const getAllLogEvents = (traceId, spanId, fromTimestampStr, toTimestampStr, page, size) => {
    console.log('getAllLogEvents');

    const params = {
        traceId: traceId || '', // Set empty string if undefined
        spanId: spanId || '',   // Set empty string if undefined
        fromTimestampStr,
        toTimestampStr,
        page,
        size
    };

    // Remove undefined or null values from params
    Object.keys(params).forEach(key => {
        if (params[key] == null) {
            delete params[key];
        }
    });

    return apiClient.get('/all', { params });
};


export const getAllLogEventsNonPaged = () => {
    console.log('getAllLogEventsNonPaged');

    // Use the apiClient for the request
    return apiClient.get('/all-nonpaged');
};
