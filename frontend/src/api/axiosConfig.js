import axios from 'axios';

const axiosInstance = axios.create({
    baseURL: 'http://localhost:8080/api',
    withCredentials: true, //Cookie
});

export default axiosInstance;