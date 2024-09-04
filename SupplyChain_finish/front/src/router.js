import Home from '@/views/Home';
import Login from '@/views/Login';
import Register from '@/views/Register'
import Company from '@/views/Company'
import Bank from '@/views/Bank'
import Individual from "@/views/Individual";

export default [
    {
        path: '/',
        redirect: '/home'
    },
    {
        path:'/home',
        component: Home
    },
    {
        path:'/login',
        component: Login
    },
    {
        path: '/register',
        component: Register
    },
    {
        path: '/company',
        component: Company
    },
    {
        path: '/bank',
        component: Bank
    },
    {
        path: '/individual',
        component: Individual
    }
]