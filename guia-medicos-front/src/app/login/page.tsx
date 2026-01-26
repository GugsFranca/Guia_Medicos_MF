import { cookies } from 'next/headers';
import { redirect } from 'next/navigation';
import LoginForm from './LoginForm';
import { decodeJwtPayload } from '../lib/decodeJwt';
import Header from '@/components/Header';
import { Footer } from '@/components/Footer';

export default async function LoginPage() {
    const cookieStore = await cookies();
    const token = cookieStore.get('auth_token')?.value;

    if (token) {
        let payload;

        try {
            payload = decodeJwtPayload(token);
        } catch (error) {
            // Em caso de erro, considera token inválido
            cookieStore.delete('auth_token');
            payload = null;
        }

        // Se chegou aqui sem lançar exceção, verifica o payload
        if (payload) {
            redirect('/admin');
        }
    }



    return (
        <div>
            <Header />
            <LoginForm />
            <Footer />
        </div>
    );
}

