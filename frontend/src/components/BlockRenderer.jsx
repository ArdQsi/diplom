import LTIBlockEditor from './LTIBlockEditor'

const BlockRenderer = ({ type }) => {
    switch (type) {
        case 'text':
            return <div className="text-block"></div>;
        case 'video':
            return (
                <div className="video-block">
                </div>
            );
        case 'lti':
            return (
                <LTIBlockEditor />
            );
        default:
            return <div>Unknown block type</div>;
    }
};

export default BlockRenderer;