import axios from 'axios';

const apiClient = axios.create({
    baseURL: 'http://localhost:3000/', // Replace with your API base URL
    timeout: 4000000 // 40 seconds timeout
});

export const getAllLogEventsNonPaged = () => {
    console.log('getAllLogEvents');

    // Use the apiClient for the request
    return apiClient.get('/all-nonpaged');
};

export const getAllLogEvents = (page = 0, size = 10000) => {
    console.log('getAllLogEvents');

    // Include pagination parameters in the API request
    const params = {
        page: page,
        size: size
    };

    return apiClient.get('/all', { params });
};