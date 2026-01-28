import axios from 'axios';

export const baseURL = '/rest/plugin/ru.slie.luna.plugins.gravity';
export const client = axios.create({baseURL});