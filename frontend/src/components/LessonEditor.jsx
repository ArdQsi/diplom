import { useState } from "react";
import Modal from 'react-modal';
import AddBlockModal from "./AddBlockModal";
import BlockRenderer from "./BlockRenderer"

const LessonEditor = () => {
    const [typeLesson, setTypeLesson] = useState("lti");
    const [modalIsOpen, setModalIsOpen] = useState(false);

    const openModal = () => {
        setModalIsOpen(true);
    };

    const closeModal = () => {
        setModalIsOpen(false);
    };

    const handleAddBlock = (blockType) => {
        setTypeLesson(blockType)
        closeModal()
    };

    return (
        <div className="lesson-editor">
            <BlockRenderer type={typeLesson} />

            <div className="add-block">
                <button
                    className="save-btn"
                    onClick={openModal}>
                    Добавить блок
                </button>
            </div>

            <Modal
                isOpen={modalIsOpen}
                onRequestClose={closeModal}
                appElement={document.getElementById('root')}>
                <AddBlockModal onAddBlock={handleAddBlock} />
            </Modal>
        </div>
    );
};

export default LessonEditor;