import { Link } from 'react-router-dom';

/** Top navigation bar shared across pages. */
export default function Navigation() {
  return (
    <header>
      <nav className="navbar navbar-expand-sm navbar-dark bg-dark border-bottom box-shadow mb-3">
        <div className="container">
          <Link className="navbar-brand" to="/">
            &#128248; Photo Album
          </Link>
          <div className="navbar-collapse collapse d-sm-inline-flex justify-content-between">
            <ul className="navbar-nav flex-grow-1">
              <li className="nav-item">
                <Link className="nav-link text-light" to="/">
                  Gallery
                </Link>
              </li>
            </ul>
          </div>
        </div>
      </nav>
    </header>
  );
}
