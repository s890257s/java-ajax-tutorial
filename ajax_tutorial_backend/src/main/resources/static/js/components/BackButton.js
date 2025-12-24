/**
 * BackButton Component
 * Renders a "Back to Home" button that redirects to the root URL.
 */
export class BackButton extends HTMLElement {
    constructor() {
        super();
        this.attachShadow({ mode: 'open' });
    }

    connectedCallback() {
        this.render();
    }

    render() {
        const style = `
            <style>
                .back-btn {
                    display: inline-flex;
                    align-items: center;
                    padding: 8px 16px;
                    background-color: #2f95efff;
                    color: white;
                    text-decoration: none;
                    border-radius: 4px;
                    font-family: 'Segoe UI', sans-serif;
                    font-size: 14px;
                    transition: background-color 0.2s;
                    margin-bottom: 20px;
                    cursor: pointer;
                    border: none;
                }
                .back-btn:hover {
                    background-color: #5a6268;
                }
                .icon {
                    margin-right: 8px;
                }
            </style>
        `;

        const html = `
            <a href="/" class="back-btn">
                <span class="icon">⬅</span>
                回首頁
            </a>
        `;

        this.shadowRoot.innerHTML = `${style}${html}`;
    }
}

customElements.define('back-button', BackButton);
