export default function Header() {

    return (
        <div>
            <header className="p-5 flex justify-center border-b-2 border-b-red-300 ">

                <nav>
                    <ul className="flex space-x-4 w-32">
                        <li><a href="/"><img src="/facil.png" alt="Marque-Fácil" className="cursor-pointer hover:scale-110" /></a></li>
                    </ul>
                </nav>
            </header>
        </div>
    );
}