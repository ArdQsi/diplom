const AddBlockModal = ({ onAddBlock }) => {
    const blockTypes = [
        { id: 'text', name: 'Тест' },
        { id: 'video', name: 'Видео' },
        { id: 'quiz', name: 'Quiz' },
        { id: 'lti', name: 'Внешний инструмент LTI' }
    ];

    return (
        <div className="modal-content">
            <h2>Добавить новый блок</h2>
            <div className="block-options">
                {blockTypes.map((block) => (
                    <div
                        key={block.id}
                        className="block-option"
                        onClick={() => onAddBlock(block.id)}
                    >
                        {block.name}
                    </div>
                ))}
            </div>
        </div>
    );
};

export default AddBlockModal;